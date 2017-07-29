import json
import os
from pprint import pprint

get_cmd='curl -H "Content-Type: application/json" -X GET -u onos:rocks http://127.0.0.1:8181/onos/v1/flows/ > flows.json'
os.system(get_cmd)

with open('flows.json') as data_file:    
    data = json.load(data_file)

for flow in data['flows']:
	flow_id=flow['id']
	device_id=flow['deviceId']
	cmd='curl -v -H "Content-Type: application/json" -X DELETE -u onos:rocks http://127.0.0.1:8181/onos/v1/flows/'+device_id+'/'+flow_id
	os.system(cmd)

os.system('sudo rm -f flows.json')
