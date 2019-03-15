import http.server
import socketserver
import os


web_dir = "C:\\Users\\TrofimovDM\\JS_projects\\backend"
os.chdir(web_dir)
PORT = 8000

Handler = http.server.SimpleHTTPRequestHandler

with socketserver.TCPServer(("", PORT), Handler) as httpd:
    print("serving at port", PORT)
    httpd.serve_forever()
