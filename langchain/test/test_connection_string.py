# Copyright (c) 2026 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at
# https://oss.oracle.com/licenses/upl/

from unittest.mock import Mock

import pytest
from borneo import AuthorizationProvider, Consistency, NoSQLHandleConfig
from borneo.kv import StoreAccessTokenProvider

from NoSqlDbConnectionString import NoSqlDbConnectionString


@pytest.mark.parametrize("uri,connection_type,auth_type,protocol", [
    ("nosqldb://us-ashburn-1", "cloud", "user_principal", "https"),
    ("nosqldb+cloud://us-ashburn-1/", "cloud", "user_principal", "https"),
    ("nosqldb://+instance_principal@us-ashburn-1/?compartment=ocid", "cloud", "instance_principal", "https"),
    ("nosqldb://+resource_principal@us-ashburn-1/?compartment=ocid", "cloud", "resource_principal", "https"),
    ("nosqldb://+oke_workload@us-ashburn-1/ocid", "cloud", "oke_workload", "https"),
    ("nosqldb://+cloudsim@localhost:8000", "cloud", "cloudsim", "https"),
    ("nosqldb+on_prem://localhost:8080", "on_prem", "user_principal", "https"),
    ("nosqldb+on_prem+http://localhost:8080", "on_prem", "user_principal", "http"),
    ("nosqldb+on_prem://user:pass@localhost:8080", "on_prem", "user_principal", "https"),
    ("nosqldb://nosql.us-newreg-1.oci.oraclecloud.com", "cloud", "user_principal", "https"),
])
def test_proposal_examples(uri, connection_type, auth_type, protocol):
    parsed = NoSqlDbConnectionString(uri)
    assert parsed.get_connection_string() == uri
    assert parsed.get_connection_type() == connection_type
    assert parsed.get_auth_type() == auth_type
    assert parsed.get_protocol() == protocol
    assert parsed.is_cloud() == (connection_type == "cloud")
    assert parsed.is_on_prem() == (connection_type == "on_prem")
    assert parsed.is_connection_type_explicit() == ("+cloud:" in uri or "+on_prem" in uri)


def test_case_escaping_path_options_and_duplicate_policy():
    parsed = NoSqlDbConnectionString(
        "NoSqLdB+ON_PREM+HtTp://Us%40er:Pa%3Ass%2B%2C%26@LOCALHOST:8080/"
        "Name%2FSpace/Table/PK?TIMEOUT=100,timeout=200&namespace=Override%20Ns"
        "&pass_phrase=a%2Bb%2Cc%26d&unknown=ignored&configFile")
    assert parsed.get_user_name() == "Us@er"
    assert parsed.get_password() == "Pa:ss+,&"
    assert parsed.get_hosts() == ["localhost:8080"]
    assert parsed.get_path() == "/Name/Space/Table/PK"
    assert parsed.get_namespace() == "Override Ns"
    assert parsed.get_options() == {
        "timeout": "100", "namespace": "Override Ns", "pass_phrase": "a+b,c&d",
        "unknown": "ignored", "configfile": ""}
    parsed.get_options()["timeout"] = "900"
    parsed.get_hosts().append("other")
    assert parsed.get_options()["timeout"] == "100"
    assert parsed.get_hosts() == ["localhost:8080"]


@pytest.mark.parametrize("service,query,expected", [
    ("cloud", "", "PathDB"),
    ("cloud", "compartment=Comp", "Comp"),
    ("cloud", "namespace=Ns", "Ns"),
    ("cloud", "namespace=Ns,compartment=Comp", "Comp"),
    ("on_prem", "", "PathDB"),
    ("on_prem", "namespace=Ns", "Ns"),
    ("on_prem", "compartment=Comp", "Comp"),
    ("on_prem", "namespace=Ns,compartment=Comp", "Ns"),
])
def test_database_options_override_path(service, query, expected):
    parsed = NoSqlDbConnectionString(f"nosqldb+{service}://localhost/PathDB/table/pk?{query}")
    assert parsed.get_namespace() == parsed.get_compartment() == expected


def test_region_resolution_protocols_default_ports_and_multiple_hosts():
    parsed = NoSqlDbConnectionString("nosqldb+http://US-ASHBURN-1,example.org:1234,[::1]/db")
    assert parsed.get_region().get_region_id() == "us-ashburn-1"
    assert parsed.get_region(1) is None
    assert parsed.get_endpoints() == [
        "http://nosql.us-ashburn-1.oci.oraclecloud.com:80",
        "http://example.org:1234", "http://[::1]:80"]
    assert NoSqlDbConnectionString("nosqldb://example.org").get_endpoint() == "https://example.org:443"
    local = NoSqlDbConnectionString("nosqldb+on_prem://us-ashburn-1")
    assert local.get_region() is None
    assert local.get_endpoint() == "https://us-ashburn-1:443"
    assert NoSqlDbConnectionString("nosqldb://+cloudsim@us-ashburn-1").get_region() is None


@pytest.mark.parametrize("uri", [
    "", "mongodb://host", "nosqldbfoo://host", "nosqldb:host", "nosqldb://",
    "nosqldb://+instance_principal@/ocid", "nosqldb://+resource_principal@/ocid",
    "nosqldb://+oke_workload@/ocid", "nosqldb://+cloudsim@",
    "nosqldb+ftp://host", "nosqldb+http+https://host", "nosqldb+cloud+cloud://host",
    "nosqldb+cloud+on_prem://host", "nosqldb+://host",
    "nosqldb+on_prem://+instance_principal@host", "nosqldb+on_prem://+cloudsim@host",
    "nosqldb://+unknown@host", "nosqldb://+resource_principal:pass@host",
    "nosqldb://host,", "nosqldb://,host", "nosqldb://host,,other",
    "nosqldb://host:", "nosqldb://host:abc", "nosqldb://host:0",
    "nosqldb://host:65536", "nosqldb://host:-1", "nosqldb://::1",
    "nosqldb://[notipv6]", "nosqldb://[::1", "nosqldb://[::1]bad",
    "nosqldb://host#fragment", "nosqldb://user:p@ss@host",
    "nosqldb://host?timeout=%ZZ", "nosqldb://ho st", "nosqldb://host\n",
    "nosqldb://user:%FF@host",
])
def test_rejects_invalid_uris(uri):
    with pytest.raises(ValueError):
        NoSqlDbConnectionString(uri)


@pytest.mark.parametrize("value", [None, 123, b"nosqldb://host", [], {}])
def test_rejects_non_strings(value):
    with pytest.raises(TypeError):
        NoSqlDbConnectionString(value)


def test_plain_username_and_optional_password_can_be_parsed():
    parsed = NoSqlDbConnectionString("nosqldb+on_prem://User@host")
    assert parsed.get_user_name() == "User"
    assert parsed.get_password() is None
    with pytest.raises(ValueError, match="both a username and password"):
        parsed.get_nosql_handle_config()


def test_constructing_parser_never_reads_credentials(monkeypatch):
    import builtins

    monkeypatch.setattr(builtins, "open", Mock(side_effect=AssertionError("Unexpected file read")))
    parsed = NoSqlDbConnectionString("nosqldb://us-ashburn-1/?configFile=/missing")
    assert parsed.get_options()["configfile"] == "/missing"


def test_on_prem_borneo_configuration_and_host_selection():
    parsed = NoSqlDbConnectionString(
        "nosqldb+on_prem+http://host1,host2:8080/PathNs?namespace=QueryNs,"
        "timeout=1234,ddlTimeout=4321,consistency=ABSOLUTE,rateLimiter=true,"
        "rateLimiterPercent=42.5,retryMaxRetries=0,pemCertificateFile=~/cert.pem,"
        "numberLib=ignored,longAsBigInt=ignored,unknown=ignored")
    config = parsed.get_nosql_handle_config(host_index=1)
    assert isinstance(config, NoSQLHandleConfig)
    assert config.get_service_url().geturl() == "http://host2:8080/"
    assert isinstance(config.get_authorization_provider(), StoreAccessTokenProvider)
    assert config.get_default_namespace() == "QueryNs"
    assert config.get_consistency() == Consistency.ABSOLUTE
    assert config.get_timeout() == 1234
    assert config.get_table_request_timeout() == 4321
    assert config.get_rate_limiting_enabled()
    assert config.get_default_rate_limiting_percentage() == 42.5
    assert config.get_retry_handler().get_num_retries() == 0
    assert config.get_ssl_ca_certs().endswith("/cert.pem")
    assert parsed.get_options()["numberlib"] == "ignored"


def test_secure_on_prem_provider(monkeypatch):
    import borneo.kv

    factory = Mock(wraps=StoreAccessTokenProvider)
    monkeypatch.setattr(borneo.kv, "StoreAccessTokenProvider", factory)
    config = NoSqlDbConnectionString("nosqldb+on_prem://User:Secret@host/Ns").get_nosql_handle_config()
    provider = config.get_authorization_provider()
    assert isinstance(provider, StoreAccessTokenProvider)
    factory.assert_called_once_with("User", "Secret")
    assert config.get_default_namespace() == "Ns"


def test_cloudsim_ignores_cloud_credentials():
    config = NoSqlDbConnectionString(
        "nosqldb+http://+cloudsim@localhost:8000/Comp?configFile=/missing,"
        "tenancy=ignored,user=ignored,security_token_file=/missing").get_nosql_handle_config()
    assert isinstance(config, NoSQLHandleConfig)
    assert config.get_service_url().geturl() == "http://localhost:8000/"
    assert config.get_default_compartment() == "Comp"
    assert config.get_authorization_provider().get_authorization_string() == "Bearer nosql"
    assert config.get_consistency() == Consistency.EVENTUAL


def test_existing_authorization_provider_bypasses_credential_loading():
    provider = Mock(spec=AuthorizationProvider)
    config = NoSqlDbConnectionString("nosqldb://us-ashburn-1/Comp?configFile=/missing").get_nosql_handle_config(provider=provider)
    assert config.get_authorization_provider() is provider
    assert config.get_default_compartment() == "Comp"
    assert config.get_service_url().hostname == "nosql.us-ashburn-1.oci.oraclecloud.com"


@pytest.fixture
def signature_factory(monkeypatch):
    import borneo.iam

    factory = Mock(return_value=Mock(spec=AuthorizationProvider))
    monkeypatch.setattr(borneo.iam, "SignatureProvider", factory)
    return factory


def test_inline_cloud_credentials(signature_factory):
    parsed = NoSqlDbConnectionString(
        "nosqldb://us-ashburn-1/?tenancy=Tenant,user=User,fingerprint=Fingerprint,"
        "key_file=~/key.pem,pass_phrase=Secret")
    parsed.get_nosql_handle_config()
    args = signature_factory.call_args.kwargs
    assert args["tenant_id"] == "Tenant"
    assert args["user_id"] == "User"
    assert args["fingerprint"] == "Fingerprint"
    assert args["private_key"].endswith("/key.pem")
    assert args["pass_phrase"] == "Secret"
    assert args["region"].get_region_id() == "us-ashburn-1"


@pytest.mark.parametrize("query,profile", [("configFile", "DEFAULT"), ("configFile=~/custom,profile=Team", "Team"), ("", "DEFAULT")])
def test_config_file_defaults_and_precedence(monkeypatch, signature_factory, query, profile):
    import oci.config

    loader = Mock(return_value={"tenancy": "FileTenant", "user": "FileUser",
                               "fingerprint": "FileFingerprint", "key_file": "~/file-key"})
    monkeypatch.setattr(oci.config, "from_file", loader)
    if query:
        query += ",tenancy=Ignored,user=Ignored,key_file=Ignored,security_token_file=/ignored"
    NoSqlDbConnectionString("nosqldb://us-ashburn-1/?" + query).get_nosql_handle_config()
    assert loader.call_args.kwargs["profile_name"] == profile
    assert loader.call_args.kwargs["file_location"].endswith("/custom" if profile == "Team" else "/.oci/config")
    assert signature_factory.call_args.kwargs["tenant_id"] == "FileTenant"
    assert signature_factory.call_args.kwargs["user_id"] == "FileUser"


@pytest.mark.parametrize("auth", ["instance_principal", "resource_principal", "oke_workload"])
def test_principal_authentication(monkeypatch, signature_factory, auth):
    import oci.auth.signers

    signer_factory = Mock(return_value=object())
    monkeypatch.setattr(oci.auth.signers, "get_oke_workload_identity_resource_principal_signer", signer_factory)
    signature_factory.create_with_instance_principal.return_value = Mock(spec=AuthorizationProvider)
    signature_factory.create_with_resource_principal.return_value = Mock(spec=AuthorizationProvider)
    NoSqlDbConnectionString(f"nosqldb://+{auth}@us-ashburn-1/?compartment=ocid").get_nosql_handle_config()
    if auth == "instance_principal":
        assert signature_factory.create_with_instance_principal.call_args.kwargs["region"].get_region_id() == "us-ashburn-1"
    elif auth == "resource_principal":
        signature_factory.create_with_resource_principal.assert_called_once_with()
    else:
        signer_factory.assert_called_once_with()
        assert signature_factory.call_args.kwargs["provider"] is signer_factory.return_value


def test_session_token_authentication(tmp_path, monkeypatch, signature_factory):
    import oci.auth.signers
    import oci.signer

    token = tmp_path / "token"
    token.write_text("session-token\n")
    key_loader = Mock(return_value=object())
    signer_factory = Mock(return_value=object())
    monkeypatch.setattr(oci.signer, "load_private_key_from_file", key_loader)
    monkeypatch.setattr(oci.auth.signers, "SecurityTokenSigner", signer_factory)
    NoSqlDbConnectionString(
        f"nosqldb://us-ashburn-1/?security_token_file={token},key_file=/key,pass_phrase=Secret").get_nosql_handle_config()
    key_loader.assert_called_once_with("/key", "Secret")
    signer_factory.assert_called_once_with("session-token", key_loader.return_value)
    assert signature_factory.call_args.kwargs["provider"] is signer_factory.return_value


@pytest.mark.parametrize("auth_source", ["inline", "config_file", "session"])
def test_real_oci_provider_with_local_credentials(tmp_path, auth_source):
    """Exercise the SDK boundary with real local keys, without network access."""
    from cryptography.hazmat.primitives import serialization
    from cryptography.hazmat.primitives.asymmetric import rsa
    from borneo.iam import SignatureProvider

    key_path = tmp_path / "private.pem"
    key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    key_path.write_bytes(key.private_bytes(
        serialization.Encoding.PEM, serialization.PrivateFormat.PKCS8,
        serialization.NoEncryption()))
    if auth_source == "inline":
        query = f"tenancy=Tenant,user=User,fingerprint=Fingerprint,key_file={key_path}"
    elif auth_source == "config_file":
        config_path = tmp_path / "oci-config"
        config_path.write_text(
            f"[DEFAULT]\ntenancy=Tenant\nuser=User\nfingerprint=Fingerprint\n"
            f"key_file={key_path}\nregion=us-phoenix-1\n")
        query = f"configFile={config_path}"
    else:
        token_path = tmp_path / "session-token"
        token_path.write_text("local-test-token\n")
        query = f"security_token_file={token_path},key_file={key_path}"
    config = NoSqlDbConnectionString("nosqldb://us-ashburn-1/Comp?" + query).get_nosql_handle_config()
    provider = config.get_authorization_provider()
    assert isinstance(provider, SignatureProvider)
    assert provider.get_region().get_region_id() == "us-ashburn-1"
    assert config.get_default_compartment() == "Comp"
    provider.close()


def test_encrypted_pem_certificate_configuration(monkeypatch):
    import ssl

    context = ssl.create_default_context()
    loader = Mock()
    monkeypatch.setattr(context, "load_cert_chain", loader)
    monkeypatch.setattr(ssl, "create_default_context", Mock(return_value=context))
    config = NoSqlDbConnectionString(
        "nosqldb+on_prem://host/?pemCertificateFile=/client.pem,pemCertificatePassword=Secret").get_nosql_handle_config()
    loader.assert_called_once_with("/client.pem", password="Secret")
    assert config.get_ssl_context() is context


def test_incomplete_inline_credentials_do_not_fall_back_to_config_file():
    with pytest.raises(ValueError, match="Missing cloud credentials: fingerprint, key_file, user"):
        NoSqlDbConnectionString("nosqldb://host/?tenancy=Tenant").get_nosql_handle_config()
    with pytest.raises(ValueError, match="requires key_file"):
        NoSqlDbConnectionString("nosqldb://host/?security_token_file=/token").get_nosql_handle_config()


@pytest.mark.parametrize("option", ["timeout=0", "timeout=no", "ddlTimeout=-1", "consistency=weak",
                                   "rateLimiter=no", "rateLimiterPercent=nan", "rateLimiterPercent=0",
                                   "retryMaxRetries=-1"])
def test_invalid_borneo_option_values(option):
    with pytest.raises(ValueError):
        NoSqlDbConnectionString("nosqldb+on_prem://localhost/?" + option).get_nosql_handle_config()


@pytest.mark.parametrize("host_index,error", [(-1, IndexError), (2, IndexError), (True, TypeError), ("0", TypeError)])
def test_invalid_host_selection(host_index, error):
    with pytest.raises(error):
        NoSqlDbConnectionString("nosqldb://host").get_endpoint(host_index)
