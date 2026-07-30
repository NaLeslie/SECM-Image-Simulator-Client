# SECM-Image-Simulator-Client
One of two programs for enabling the modified deconvolution of scanning electrochemical micoscopy (SECM) images for obtaining the rate constant as a function of position at a surface from an SECM image.
This is the client program which connects to the [SECM-Image-Simulator-Server](https://github.com/NaLeslie/SECM-Image-Simulator-Server) for instructions on what SECM images to simulate next.
The communication occurs via HTTP messages over the loopback address at a port defined by `SecmDeconvClient.PORT`
There are two versions for this program which can be found as the two branches: `master` and `derivative` for use with data transforms that <b>do not</b> and <b>do</b> require partial derivatives of current with respect to rate constant

## Usage
Linux commands shown here. COMSOL is called from the console slightly differently on Windows.
- Run the [SECM-Image-Simulator-Server](https://github.com/NaLeslie/SECM-Image-Simulator-Server) jar `java -jar SECM-Image_Simulator-Server.jar > srvlog.log`
- Edit `filepath = ""` in the main method of the example implementation to the desired instruction file.
- Compile: `comsol compile SecmDeconvClient.java`
- Run: `comsol batch -inputfile SecmDeconvClient.class -batchlog batlog.log > runlog.log`

Note: you will need to enable the following in COMSOL's `Preferences > Security > Methods and Java Libraries` menu:
- Allow access to system properties
- File system access: All files
- Allow access to network sockets

### Instruction file
Instruction files have the following format:  
```
##ENCODING: csv
##X-Sampling: StartIndex,StepSize,NumberOfSteps
#22,4,17
##Y-Sampling: StartIndex,StepSize,NumberOfSteps
#9,4,17
##xindex,yindex,xcoord/m,ycoord/m,current/A
0,0,0.0,0.0,9.228357295097126E-10
0,1,0.0,2.0E-6,9.22923353766823E-10
 ...
 ```

## Publication
This work is associated with the publication:  
Leslie, N.; Mauzeroll, J. Quantification of Kinetics from SECM Images Using a Modified Deconvolution Algorithm. Electrochimica Acta  
which is currently under peer review.

## Data Transmission
Image `(x,y,signal)` and curve `(x,signal)` data are exchanged between the programs using HTTP messages, but state is held by the server program, so this process is not RESTful.

![Flowchart of data flow between client and server programs.](https://github.com/NaLeslie/SECM-Image-Simulator-Server/blob/main/Data-pipeline.svg)

### Resources:
The communication protocol between the 'client' and 'server' is a mostly HTTP/1.1 compliant protocol.
It has some specific expectations for the 'ETag' and 'Content-Encoding' header fields (many requests that lack `Content-Encoding: 0.2' in the header receive `400 BAD REQUEST' responses).
The protocol has 5 resources which respond to GET, HEAD, and PUT requests.
- **k-curve**: Stores the log*k*-*i* curve (*k*-curve) of the microelectrode.
- **target-image**: Stores the true SECM image (*i*-image).
- **k-image**: Stores the latest rate constant image (*k*-image) estimate.
- **derivative-image**: Stores the latest image of ∂*i*/∂log*k* (Only used for TaylorExpansionTransform).
- **secm-image**: Stores the latest *i*-image. POST requests to this resource starts an iteration of deconvolution and the response contains the next *k*-image estimate as the message body.  

### Formatting:
#### Curve:
The *k* curve is represented by a double array containing the *x*-data and a second double array containing the *y*-data.
The arrays are converted to byte arrays by encoding the each double into 8 bytes (little endian).
The binary *y* data is hashed with SHA-256, the hexadecimal representation of which is used in the 'ETag' header field.
The encoded *k*-curve will become the message body and its length in bytes will be used by the 'Content-Length' header field.  
The *k*-curve is encoded according to:
```
"len: " + [Number of data points (in ASCII)] + "\r\n"  
[Binary data containing logks] + "\r\n"  
[Binary data containing currents] + "\r\n"
```

#### Image:
Image data is represented by a double array containing the *x* coordinates, another double array containing the *y* coordinates and a 2D double array containing the image data.
Image data is unravelled into a long array before being converted to binary.
The 2D double array gets converted to a 1D array by sticking slices in the *y* direction one after another such that `unraveled_image[x*ylen + y] = xy_image[x][y]`.
The 'ETag' field data is the SHA-256 hash of the unraveled image in hexadecimal.
The image data is encoded in the message body and its length in bytes is used in the 'Content-Length' header field.  
The image data is encoded according to:
```
"xlen: " + [ Number of x points (in ASCII)] + "\r\n"  
"ylen: " + [ Number of y points (in ASCII)] + "\r\n"  
[Binary data containing x coordinates] + "\r\n"  
[Binary data containing y coordinates] + "\r\n"  
[Binary data containing unraveled image signals] + "\r\n"
```
