import urllib.request
import urllib.error
import json


def handler(event: dict, context) -> dict:
    """Проверяет существование ника в Minecraft через Mojang API"""
    
    if event.get('httpMethod') == 'OPTIONS':
        return {
            'statusCode': 200,
            'headers': {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type',
                'Access-Control-Max-Age': '86400'
            },
            'body': ''
        }

    params = event.get('queryStringParameters') or {}
    nick = (params.get('nick') or '').strip()

    if not nick:
        return {
            'statusCode': 400,
            'headers': {'Access-Control-Allow-Origin': '*'},
            'body': json.dumps({'valid': False, 'reason': 'empty'})
        }

    if len(nick) < 3 or len(nick) > 16:
        return {
            'statusCode': 200,
            'headers': {'Access-Control-Allow-Origin': '*'},
            'body': json.dumps({'valid': False, 'reason': 'length'})
        }

    url = f'https://api.mojang.com/users/profiles/minecraft/{nick}'
    req = urllib.request.Request(url, headers={'User-Agent': 'FortressCraft/1.0'})

    try:
        with urllib.request.urlopen(req, timeout=5) as resp:
            data = json.loads(resp.read())
            return {
                'statusCode': 200,
                'headers': {'Access-Control-Allow-Origin': '*'},
                'body': json.dumps({'valid': True, 'name': data.get('name', nick)})
            }
    except urllib.error.HTTPError as e:
        if e.code == 404:
            return {
                'statusCode': 200,
                'headers': {'Access-Control-Allow-Origin': '*'},
                'body': json.dumps({'valid': False, 'reason': 'not_found'})
            }
        raise
