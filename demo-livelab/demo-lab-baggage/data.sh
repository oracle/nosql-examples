mkdir ~/BaggageData
cd ~/BaggageData
curl https://raw.githubusercontent.com/dario-vega/ndcs_baggage_tracking_demo/main/data/generated_data/BaggageData.tar.gz -o BaggageData.tar.gz
tar xvzf BaggageData.tar.gz
rm  BaggageData.tar.gz
