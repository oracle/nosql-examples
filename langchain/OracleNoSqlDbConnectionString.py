# Copyright (c) 2026 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at
# https://oss.oracle.com/licenses/upl/

"""Parse NoSQL Database connection URIs and create Borneo configurations."""

from ipaddress import IPv6Address
from os.path import expanduser
from typing import TYPE_CHECKING
from urllib.parse import parse_qsl, unquote, urlsplit
import re

if TYPE_CHECKING:
    from borneo import AuthorizationProvider, NoSQLHandleConfig


class OracleNoSqlDbConnectionString:
    """A parsed NoSQL Database connection string.

    Format::

        nosqldb[+cloud|+on_prem][+http|+https]://[auth@]hosts[/database[/path]][?options]

        - nosqldb[+connection_type][+protocol] - the scheme. New schemes should be registered with the Internet Assigned Numbers Authority (IANA), although non-registered schemes are used in practice, like mongodb or dse.
            - nosqldb - required, error if not present
            - connection_type - on_prem|cloud - optional, cloud is default if not specified
            - protocol - http|https - optional, https is default if not specified
        - auth:  - authentication type, optional - user auth by default if not specified
            - username[:password] - user name and optional password, strongly advised not to be used in production but it is included here for simplicity at develop time
            - +instance_principal | +resource_principal | +oke_workload - valid only if connection_type is cloud
            - +cloudsim - valid with cloud for connection_type, using this option will ignore cloud auth options, ex: configFile,tenancy,user, etc. 
        - host1[:port1][, ...hostN[:portN]] - at least one hostname is required, depending on the service type (see below) these values have the following semantics:
            - host - if connection_type is cloud, host will be looked for in the list of known region identifiers. Example: us-ashburn-1
                    - if it is not found as a region identifier it is interpreted as a direct endpoint
            - port - optional, if not specified it's 443 for https and 80 for http.
        - path - path to the DB entity, starts with the default compartment_id or namespace to be used and followed by optional path steps reserved for future extensions (ex: namespace|compartment/table_name/pk), ignored if not known
        -  paramspec - optional list of options: option_name1[=option_value1][,...option_nameN=option_nameN], ignored if not known
            - for cloud:
                - configFile?[=~/.oci/config] - specifies the location of the config file, if value not specified the default ~/.oci/config location is used
                - profile?=profile_name - specifies the profile name to be used from config file, if not specified DEFAULT profile is used.
                - all entries allowed in config file other then region:(if configFile option is specified the following are ignored)
                    - tenancy=tenancy_id - specifies the OCID of your tenancy.
                    - user=user_id - specifies the OCID of the user calling the API. 
                    - fingerprint=fingerprint_value - specifies the fingerprint for the public key that was added to this user.
                    - key_file=full_path - specifies the full path and filename of the private key.
                    - pass_phrase=passphrase - specifies the passphrase used for the key, if it is encrypted.
                    - security_token_file?=file_name - If session token authentication is being used, then this parameter is required.
            - for cloudsim - ideally all cloud options would work and be ignored if not applicable
            - for on_prem:
                - namespace?=string - The default namespace to use. Note: compartment option can be used instead.
                - pemCertificateFile?=fileName
                - pemCertificatePassword?=passphrase
            - for all service types (all of these are defined here): 
                - compartment?=string - The default compartment to use. Note: namespace option can be used instead.
                - adminPollDelay?=number 
                - adminPollTimeout?=number
                - consistency?=EVENTUAL(default) | ABSOLUTE
                - numberLib?=string - for NodeJS SDK only - number library to use
                - ddlTimeout?=number
                - durabilityMasterSync?=string - if not set: durability on server is used, else: NO_SYNC | SYNC | WRITE_NO_SYNC
                - durabilityReplicaAck?=string - if not set: durability on server is used, else: ALL | NONE | SIMPLE_MAJORITY
                - durabilityReplicaSync?=string - if not set: durability on server is used, else: NO_SYNC | SYNC | WRITE_NO_SYNC
                - httpOpt?  - Http/Https options
                - longAsBigInt?=boolean - for NodeJS SDK only, otherwise ignored
                - maxMemoryMB?=number 
                - rateLimiter?=string | boolean
                - rateLimiterPercent?=number
                - retryBaseDelay?=number
                - retryControlOpBaseDelay?=number
                - retryMaxRetries?=number
                - retrySecInfoBaseDelay?=number
                - retrySecInfoNumBackoff?=number
                - retrySecurityInfoTimeout?=number
                - tablePollDelay?=number
                - tablePollTimeout?=number
                - timeout?=number
        
        Examples:

            nosqldb://us-ashburn-1 or nosqldb+cloud://us-ashburn-1/ - connect to cloud service at Ashburn region using ~/.oci/config file for rest of options
            nosqldb://+instance_principal@us-ashburn-1/?compartment=ocid - for instance principal auth
            nosqldb://+resource_principal@us-ashburn-1/?compartment=ocid - for resource principal auth
            nosqldb://+cloudsim@localhost:8000 - connect to cloudsim on localhost:8000
            nosqldb+on_prem://localhost:8080 - connect to on-prem at http://localhost:8080
            nosqldb+on_prem+http://localhost:8080 - connect to on-prem at http://localhost:8080
            nosqldb+on_prem://user:pass@localhost:8080 - connect to on-prem at https://localhost:8080 with user:pass

    Parsing does not read credential files or connect to a database. Call
    :meth:`get_nosql_handle_config` to create a ``borneo.NoSQLHandleConfig``. Option names,
    scheme components and hostnames are case insensitive; credential values
    and path components retain their case. Duplicate options use the first
    value. Unknown options, Node.js-only options and options without a Python
    handle configuration equivalent are retained by :meth:`get_options` and
    ignored by :meth:`get_nosql_handle_config`.
    """

    _PRINCIPALS = {"instance_principal", "resource_principal", "oke_workload"}
    _AUTH_TYPES = _PRINCIPALS | {"cloudsim"}

    def __init__(self, connection_string: str) -> None:
        if not isinstance(connection_string, str):
            raise TypeError("NoSQL DB connection string must be a string.")
        if not connection_string:
            raise ValueError("NoSQL DB connection string cannot be empty.")
        if any(char.isspace() or ord(char) < 32 for char in connection_string):
            raise ValueError("Whitespace in a connection string must be percent encoded.")
        if re.search(r"%(?![0-9a-fA-F]{2})", connection_string):
            raise ValueError("Invalid percent escape in NoSQL DB connection string.")

        scheme, separator, remainder = connection_string.partition("://")
        parts = scheme.lower().split("+")
        if not separator or parts[0] != "nosqldb":
            raise ValueError("NoSQL DB connection string must start with nosqldb://.")

        connection_type = protocol = None
        for part in parts[1:]:
            if part in {"cloud", "on_prem"} and connection_type is None:
                connection_type = part
            elif part in {"http", "https"} and protocol is None:
                protocol = part
            else:
                raise ValueError("Invalid or repeated connection type or protocol in scheme.")

        # A standard URL parser cannot parse a list of IPv6/hostname endpoints
        # or a scheme containing on_prem. Parse the authority separately.
        authority, rest = re.fullmatch(r"([^/?#]*)(.*)", remainder).groups()
        url = urlsplit("nosqldb://placeholder" + rest)
        if "#" in remainder:
            raise ValueError("Fragments are not supported in NoSQL DB connection strings.")

        self._connection_string = connection_string
        self._connection_type = connection_type or "cloud"
        self._connection_type_explicit = connection_type is not None
        self._protocol = protocol or "https"
        self._auth_type = "user_principal"
        self._username = self._password = None
        if "@" in authority:
            if authority.count("@") != 1:
                raise ValueError("An @ in credentials must be percent encoded.")
            auth, authority = authority.split("@", 1)
            username, colon, password = auth.partition(":")
            self._username = unquote(username, errors="strict")
            self._password = unquote(password, errors="strict") if colon else None
            if self._username.startswith("+"):
                auth_type = self._username[1:].lower()
                if auth_type not in self._AUTH_TYPES:
                    raise ValueError("Unknown NoSQL DB authentication type.")
                if self._connection_type != "cloud":
                    raise ValueError("Principal and cloudsim authentication require cloud.")
                if colon:
                    raise ValueError("Principal and cloudsim authentication do not accept passwords.")
                self._auth_type = auth_type
                self._username = None

        self._hosts = tuple(self._parse_host(host) for host in authority.split(",")) if authority else ()
        if not self._hosts:
            raise ValueError("NoSQL DB connection string must contain a host name.")

        self._path = unquote(url.path, errors="strict")
        self._database = unquote(url.path.split("/")[1], errors="strict") if url.path else ""
        self._options: dict[str, str] = {}
        # Both the proposal's comma separator and conventional URI '&' work.
        for name, value in parse_qsl(url.query.replace(",", "&"), keep_blank_values=True,
                                     encoding="utf-8", errors="strict"):
            if name:
                self._options.setdefault(name.lower(), value)

    @staticmethod
    def _parse_host(host: str) -> str:
        if not host:
            raise ValueError("Host list cannot contain empty entries.")
        port = None
        if host.startswith("["):
            address, bracket, suffix = host[1:].partition("]")
            if not bracket or (suffix and not suffix.startswith(":")):
                raise ValueError("Invalid IPv6 endpoint.")
            try:
                IPv6Address(address)
            except ValueError:
                raise ValueError("Invalid IPv6 address.") from None
            if suffix:
                port = suffix[1:]
        else:
            address, colon, value = host.partition(":")
            if not re.fullmatch(r"[A-Za-z0-9_.-]+", address):
                raise ValueError("Invalid hostname; IPv6 addresses must be bracketed.")
            if colon:
                port = value
        if port is not None and (not re.fullmatch(r"[0-9]+", port) or not 1 <= int(port) <= 65535):
            raise ValueError("Port must be an integer between 1 and 65535.")
        return host.lower()

    def get_connection_string(self) -> str:
        return self._connection_string

    def is_connection_type_explicit(self) -> bool:
        return self._connection_type_explicit

    def get_connection_type(self) -> str:
        return self._connection_type

    def is_cloud(self) -> bool:
        return self._connection_type == "cloud"

    def is_on_prem(self) -> bool:
        return self._connection_type == "on_prem"

    def get_protocol(self) -> str:
        return self._protocol

    def get_auth_type(self) -> str:
        return self._auth_type

    def get_user_name(self) -> str | None:
        return self._username

    def get_password(self) -> str | None:
        return self._password

    def get_hosts(self) -> list[str]:
        return list(self._hosts)

    def get_region(self, host_index: int = 0):
        """Return the Borneo Region for a known cloud region, otherwise None."""
        from borneo import Regions

        host = self._host_parts(host_index)
        if self.is_cloud() and self._auth_type != "cloudsim":
            return Regions.from_region_id(host.hostname)
        return None

    def _host_parts(self, host_index: int):
        if isinstance(host_index, bool) or not isinstance(host_index, int):
            raise TypeError("host_index must be an integer.")
        if not 0 <= host_index < len(self._hosts):
            raise IndexError("host_index is outside the host list.")
        return urlsplit("https://" + self._hosts[host_index])

    def get_endpoint(self, host_index: int = 0) -> str:
        """Resolve a host or region to an endpoint, including the default port."""
        host = self._host_parts(host_index)
        region = self.get_region(host_index)
        hostname = urlsplit(region.endpoint()).hostname if region else host.hostname
        if ":" in hostname:
            hostname = f"[{hostname}]"
        port = host.port or (443 if self._protocol == "https" else 80)
        return f"{self._protocol}://{hostname}:{port}"

    def get_endpoints(self) -> list[str]:
        """Return resolved endpoints in URI order; no connection is attempted."""
        return [self.get_endpoint(index) for index in range(len(self._hosts))]

    def get_options(self) -> dict[str, str]:
        """Return a copy of options keyed by lowercase names."""
        return dict(self._options)

    def get_path(self) -> str:
        return self._path

    def get_compartment(self) -> str:
        return self._get_database()

    def get_namespace(self) -> str:
        return self._get_database()

    def _get_database(self) -> str:
        primary, alias = ("compartment", "namespace") if self.is_cloud() else ("namespace", "compartment")
        return self._options.get(primary, self._options.get(alias, self._database))

    def get_nosql_handle_config(
        self, *, provider: "AuthorizationProvider | None" = None,
        host_index: int = 0,
    ) -> "NoSQLHandleConfig":
        """Create a Borneo ``NoSQLHandleConfig`` for the selected host.

        This does not connect to the database. Creating an IAM provider can
        read credential files or contact the principal's identity service.
        Supply ``provider`` to use an existing authorization provider instead.
        Borneo accepts one endpoint per handle; ``host_index`` selects an entry
        from a multi-host URI. No automatic failover is performed.

        Applied handle options: compartment/namespace, consistency, timeout,
        ddlTimeout, rateLimiter, rateLimiterPercent, retryMaxRetries and TLS
        certificates. Other options are available from ``get_options()`` for
        application-level polling, requests, or SDK-specific handling.
        """
        from borneo import Consistency, DefaultRetryHandler, NoSQLHandleConfig

        endpoint = self.get_endpoint(host_index)
        if provider is None:
            provider = self._create_provider(host_index)
        config = NoSQLHandleConfig(endpoint, provider)
        database = self._get_database()
        if database:
            if self.is_on_prem():
                config.set_default_namespace(database)
            else:
                config.set_default_compartment(database)

        options = self._options
        consistency = options.get("consistency", "EVENTUAL").upper()
        if consistency not in {"EVENTUAL", "ABSOLUTE"}:
            raise ValueError("consistency must be EVENTUAL or ABSOLUTE.")
        config.set_consistency(getattr(Consistency, consistency))
        for option, setter in (("timeout", config.set_timeout),
                               ("ddltimeout", config.set_table_request_timeout)):
            if option in options:
                setter(self._integer_option(option, minimum=1))
        if "ratelimiter" in options:
            value = options["ratelimiter"].lower()
            if value not in {"true", "false"}:
                raise ValueError("Borneo rateLimiter must be true or false.")
            config.set_rate_limiting_enabled(value == "true")
        if "ratelimiterpercent" in options:
            try:
                percent = float(options["ratelimiterpercent"])
            except ValueError:
                raise ValueError("rateLimiterPercent must be a positive number.") from None
            if not 0 < percent < float("inf"):
                raise ValueError("rateLimiterPercent must be a positive number.")
            config.set_default_rate_limiting_percentage(percent)
        if "retrymaxretries" in options:
            config.set_retry_handler(DefaultRetryHandler(
                retries=self._integer_option("retrymaxretries", minimum=0)))

        if self.is_on_prem() and "pemcertificatefile" in options:
            import ssl

            certificate = expanduser(options["pemcertificatefile"])
            password = options.get("pemcertificatepassword")
            if password is None:
                config.set_ssl_ca_certs(certificate)
            else:
                # An encrypted client certificate/key needs an SSLContext.
                context = ssl.create_default_context()
                context.load_cert_chain(certificate, password=password)
                config.set_ssl_context(context)
        return config

    def _integer_option(self, name: str, *, minimum: int) -> int:
        value = self._options[name]
        if not re.fullmatch(r"[0-9]+", value) or int(value) < minimum:
            raise ValueError(f"{name} must be an integer >= {minimum}.")
        return int(value)

    def _create_provider(self, host_index: int):
        if self.is_on_prem():
            from borneo.kv import StoreAccessTokenProvider

            if self._username is None and self._password is None:
                return StoreAccessTokenProvider()
            if not self._username or not self._password:
                raise ValueError("Borneo requires both a username and password for secure on-prem connections.")
            return StoreAccessTokenProvider(self._username, self._password)

        if self._auth_type == "cloudsim":
            from borneo import AuthorizationProvider

            class CloudsimAuthorizationProvider(AuthorizationProvider):
                def close(self):
                    pass

                def get_authorization_string(self, request=None):
                    return "Bearer nosql"

            return CloudsimAuthorizationProvider()

        from borneo.iam import SignatureProvider

        region = self.get_region(host_index)
        if self._auth_type == "instance_principal":
            return SignatureProvider.create_with_instance_principal(region=region)
        if self._auth_type == "resource_principal":
            return SignatureProvider.create_with_resource_principal()
        if self._auth_type == "oke_workload":
            from oci.auth.signers import get_oke_workload_identity_resource_principal_signer

            signer = get_oke_workload_identity_resource_principal_signer()
            return SignatureProvider(provider=signer, region=region.get_region_id() if region else None)

        options = self._options
        credential_names = {"tenancy", "user", "fingerprint", "key_file", "pass_phrase", "security_token_file"}
        if "configfile" in options or not credential_names.intersection(options):
            from oci.config import from_file

            credentials = from_file(
                file_location=expanduser(options.get("configfile") or "~/.oci/config"),
                profile_name=options.get("profile") or "DEFAULT")
        else:
            credentials = options

        if credentials.get("security_token_file"):
            from oci.auth.signers import SecurityTokenSigner
            from oci.signer import load_private_key_from_file

            if not credentials.get("key_file"):
                raise ValueError("Session token authentication requires key_file.")
            with open(expanduser(credentials["security_token_file"]), encoding="utf-8") as token_file:
                token = token_file.read().strip()
            private_key = load_private_key_from_file(
                expanduser(credentials["key_file"]), credentials.get("pass_phrase"))
            return SignatureProvider(
                provider=SecurityTokenSigner(token, private_key),
                region=region.get_region_id() if region else None)

        required = {"tenancy", "user", "fingerprint", "key_file"}
        missing = sorted(name for name in required if not credentials.get(name))
        if missing:
            raise ValueError("Missing cloud credentials: " + ", ".join(missing))
        return SignatureProvider(
            tenant_id=credentials["tenancy"], user_id=credentials["user"],
            fingerprint=credentials["fingerprint"],
            private_key=expanduser(credentials["key_file"]),
            pass_phrase=credentials.get("pass_phrase"), region=region)
