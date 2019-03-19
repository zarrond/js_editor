from http.server import BaseHTTPRequestHandler, HTTPServer
import os


# web_dir = "C:\\Users\\TrofimovDM\\JS_projects\\backend"
# os.chdir(web_dir)


class S(BaseHTTPRequestHandler):

    def _set_response(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/plain')
        self.end_headers()

    def do_GET(self):
        print("GET request,\nPath: {}\nHeaders:\n{}\n".format( str(self.path), str(self.headers)))
        print(self.path)
        try:
            with open(self.path, 'rb') as f:
                self._set_response()
                self.wfile.write(f.read())
        except FileNotFoundError:
            self.send_response(404)
            self.send_header('Content-type', 'text/plain')
            self.end_headers()
            self.wfile.write("File not fount".format(self.path).encode('utf-8'))



    #self.wfile.write("GET request44 for {}".format(self.path).encode('utf-8'))

    def do_POST(self):
        content_length = int(self.headers['Content-Length']) # <--- Gets the size of data
        post_data = self.rfile.read(content_length) # <--- Gets the data itself
        print("POST request,\nPath: {}\nHeaders:\n{}\n\nBody:\n{}\n".format(
                str(self.path), str(self.headers), post_data.decode('utf-8')))

        self._set_response()
        self.wfile.write("POST request for {}".format(self.path).encode('utf-8'))


def run(server_class=HTTPServer, handler_class=S, port=8080):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    print('Starting httpd...\n')
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    print('Stopping httpd...\n')


if __name__ == '__main__':
    print(os.path.realpath(__file__))
    from sys import argv

    if len(argv) == 2:
        run(port=int(argv[1]))
    else:
        run()

