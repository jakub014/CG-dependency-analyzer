import json
import os

#v_dir = '/mnt/fasten/vuln/consumer/lima-statements/'
v_dir = 'd:/Niels/tmp_fasten_vuln/lima-statements/'
o_path = 'output2.txt'

for v_filename in os.listdir(v_dir):

    # For each file in v_dir, open and load data
    v_filepath = os.path.join(v_dir, v_filename)
    v_file = open(v_filepath, )
    data = None
    data = json.load(v_file)


    # If vulnerable_fasten_uris is non-empty, append contents to output file
    if data['vulnerable_fasten_uris'] is not None \
            and len(data['vulnerable_fasten_uris']) > 0:
        v_obj = { "id":data['id'], 'severity': data['severity'], 'vulnerable_purls': data['vulnerable_purls'], 'vulnerable_fasten_uris': data['vulnerable_fasten_uris'], 'references': data['references']}
        o_file = open(o_path, 'a')
        o_file.write(json.dumps(v_obj, indent=4) + '\n')
        o_file.close()
    v_file.close()