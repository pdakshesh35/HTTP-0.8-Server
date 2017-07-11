# HTTP 0.8 Server
javac -cp . SimpleHTTPServer.java

java -cp . SimpleHTTPServer 3456

- Accept the port as args[0] in the main method.  The port should be parsed into an int or Integer.
- Construct a ServerSocket that accepts connections on the port specified in the command-line argument.
- When a client connects to your ServerSocket, you are to spawn a new thread to handle all the client's communication. Absolutely no reading or writing to or from a Socket should occur in the class that the ServerSocket is accepting connections in.
- Your communication thread should be a properly-constructed Java thread.
- In your thread you should read a single String from the client, parse it as an HTTP 0.8 request and send back an appropriate response according to the HTTP 0.8 protocol below.
- Your responses should correctly refer to the resource directory supplied, see below for more details.
- Once your response has been sent, you should flush() your output streams, wait a quarter second, close down all communication objects and cleanly exit the thread.
- Be careful to handle Exceptions intelligently. No Exceptions should be capable of crashing your server.

HTTP 0.8

Requests:
HTTP 0.8 is a subset of the HTTP 0.9 protocol. An HTTP 0.8 request is structured as:

<command> <resource>

Where <command> is a series of capital letters
Where <resource> is the full path to the requested file, beginning with a '/'. For instance, a request for the file 'index.html' that is in the server's root directory would look like:

GET /index.html

The <command> and <resource> must be separated by a space.
There must be a newline at the end of the request.


Responses:
Every HTTP 0.8 request must be given a HTTP 0.8 response string. There are only 6 valid HTTP 0.8 response strings:

400 Bad Request:
When the request is formatted improperly. Any request that does not fit another response type is a 'bad request'. For instance, "GET/index.html" is a bad request because it is missing a space between the command and resource. The request "GET" is a bad request because there is no resource named. The request "" is also a bad request.

404 Not Found:
When the request is properly formatted, but the requested resource doesn't exist. For instance, if the file 'magic.html' does not exist, "GET /magic.html" should result in a 404 response. It is properly formed, so it is not a bad requet, but the resource requested does not exist.

501 Not Implemented:
When the request is properly formatted, but the command is not "GET". For instance, if the request is "POST /index.html", it is a properly formatted request, but for a command that is not implemented.

500 Internal Error:
If your program encounters an error that is not a problem with the request, but your program itself, that makes it impossible to get the resource, you must return this status code. If you can not access a file, read a resource, or if you have any type of error or Exception that prevents you from handling the request, you should send this code.

200 OK:
If everything goes right, you return this status code. If the request is properly formatted, the resource exists and there is no problem reading it and preparing it to be sent, you should respond with this code. This code is different from the others because it consists of two parts. The first is '200 OK', then, after two newlines (one blank line), you should also send the requested data. For instance, if the file '/gettysburgAddress.txt' was requested, and that file existed in that location on the server, the response should be:

200 OK

Fourscore and seven years ago ...


Be careful not to send only '200 OK'. You must read in the entire resource requested from the attached resource zip file and send it along with the response in order for the response to be complete.

