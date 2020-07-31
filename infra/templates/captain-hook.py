#!/usr/bin/env python3
# based on
import os, argparse
from datetime import datetime, timedelta
from flask import Flask, request, abort, jsonify
from subprocess import check_output

def temp_token():
    import binascii
    temp_token = binascii.hexlify(os.urandom(24))
    return temp_token.decode('utf-8')

WEBHOOK_VERIFY_TOKEN = os.getenv('WEBHOOK_VERIFY_TOKEN')
CLIENT_AUTH_TIMEOUT = 24 # in Hours

app = Flask(__name__)

authorised_clients = {}

@app.route('/webhook', methods=['GET', 'POST'])
def webhook():
    if request.method == 'GET':
        verify_token = request.args.get('verify_token')
        if verify_token == WEBHOOK_VERIFY_TOKEN:
            authorised_clients[request.remote_addr] = datetime.now()
            #p = subprocess.run("echo 'hello world!'", capture_output=True, shell=True, encoding="utf8")
            ## assert p.stdout == 'hello world!\n'
            out = check_output(["docker-compose", "version"])
            print(out.decode('ascii'))
            return jsonify({'status':'success','result':out.decode('ascii')}), 200
        else:
            return jsonify({'status':'bad token'}), 401

    # elif request.method == 'POST':
    #     client = request.remote_addr
    #     if client in authorised_clients:
    #         if datetime.now() - authorised_clients.get(client) > timedelta(hours=CLIENT_AUTH_TIMEOUT):
    #             authorised_clients.pop(client)
    #             return jsonify({'status':'authorisation timeout'}), 401
    #         else:
    #             print(request.json)
    #             return jsonify({'status':'success'}), 200
    #     else:
    #         return jsonify({'status':'not authorised'}), 401

    else:
        abort(400)

if __name__ == '__main__':
    if WEBHOOK_VERIFY_TOKEN is None:
        print('WEBHOOK_VERIFY_TOKEN has not been set in the environment.\nGenerating random token...')
        token = temp_token()
        print('Token: %s' % token)
        WEBHOOK_VERIFY_TOKEN = token
    else:
        print(f'Using WEBHOOK_VERIFY_TOKEN from environment len={len(WEBHOOK_VERIFY_TOKEN)}')
    parser = argparse.ArgumentParser()
    parser.add_argument('-s', '--ssl', dest='ssl_enabled', default=False, action='store_true',help="run in ssl mode")
    parser.add_argument('-p', '--port', dest='port', default=5000,help="listener port (default 5000)")
    args = parser.parse_args()
    if args.ssl_enabled:
        print(f'Running with ssl_enabled={args.ssl_enabled} port={args.port}')
        certdir= os.path.join('/etc/letsencrypt/live/','${certbot_domain_name}')
        app.run(host='0.0.0.0',port=args.port, ssl_context=(os.path.join(certdir,'fullchain.pem'),os.path.join(certdir,'privkey.pem')))
    else:
        print(f'Running in http only mode port={args.port}')
        app.run(host='0.0.0.0',port=args.port)
