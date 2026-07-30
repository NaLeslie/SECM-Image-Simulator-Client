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

## Data Transmission
Image `(x,y,signal)` and curve `(x,signal)` data are exchanged between the programs using HTTP messages, but state is held by the server program, so this process is not RESTful.
### 
### Formatting:
#### Curve
"len: " + \[Number of data points (in ASCII)\] + "\r\n"  
\[Binary data containing logks\] + "\r\n"  
\[Binary data containing currents\] + "\r\n"

#### Image:
"xlen: " + \[ Number of x points (in ASCII) \] + "\\ r \\n"  
"ylen: " + \[ Number of y points (in ASCII) \] + "\\ r \\n"  
\[ Binary data containing x coordinates \] + "\\ r \\n"  
\[ Binary data containing y coordinates \] + "\\ r \\n"  
\[ Binary data containing unraveled image signals \] + "\\ r \\n"

## Publication
This work is associated with the publication:  
Leslie, N.; Mauzeroll, J. Quantification of Kinetics from SECM Images Using a Modified Deconvolution Algorithm. Electrochimica Acta  
which is currently under peer review.
