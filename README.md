# WS-PGRADE Extensions
## What are these extensions?
### The `ApplicationManager` Portlet
[WS-PGRADE] is able to communicate with several resource managers, and these managers often lack an application database of sorts. [UNICORE] excels in this aspect and keeps all applications registered in an _incarnation database_, but this seems to be the exception.

This portlet lets the administrator of the [WS-PGRADE] portal to register applications, thus, extending [WS-PGRADE] by adding its own _application database_.

## Required development tools
You will need Java 1.8 and [maven]. Plus, if you want to deploy the files yourself, you will also need Apache ant.

## Why is there a dependencies folder?
Java repositories should not contain binaries, but at some point between the creation of this project and the release of it, one of the repositories holding required dependencies was not reachable anymore. Sadly, code that is not maintained by us refers to a repository that is not reachable, so we are distributing the needed files in the `dependencies` folder.

## How can I install the dependencies?
Go to the `dependencies` folder and execute the following commands to install the dependencies in your local repository:

    $ mvn install:install-file -DpomFile=portal-service.pom -Dfile=portal-service.jar
    $ mvn install:install-file -DpomFile=unicore6-api.pom -Dfile=unicore6-api.jar
    $ mvn install:install-file -DpomFile=externals.pom -Dfile=externals.pom
    $ mvn install:install-file -DpomFile=unreleased.pom -Dfile=unreleased.pom

## How can I build the extensions?
To build the extensions, simply run:

    $ mvn package
    
## Is there an installation process?
No. You just need to install the generated portlets into your [WS-PGRADE] instance by following [WS-PGRADE] user's manual. 

## How can I install the extensions?
There are two ways in which you can deploy the portlets on a [WS-PGRADE] instance. Regardless of which way you choose, you need to restart the [WS-PGRADE] portal.

### Manually deploying the portlets
Once you've built the portlets, `ApplicationManager.war`, `WorkflowImporter.war`, you will need to log-in to your [WS-PGRADE] instance as an administrator, then, go through the following steps:

* Access the control panel 

![Alt "Control Panel"][deploy1]

* Locate the _App Manager_ in the control panel bar: 

![Alt "Locating the App Manager"][deploy2]

* Click on _Install_: 

![Alt "Clicking on Install in the App Manager"][deploy3]

* Select _File upload_ and then click on the _Choose file_ button: 

![Alt "Uploading a portlet"][deploy4]

* Navigate to the folder on which the portlets were built (typically, under the `target` folder), select one of the portlets and click on the _Install_ button. You will need to repeat this step for each portlet.

* Restart the [WS-PGRADE] portal.

### Deploy the portlets as part of the build process
[maven]'s job finishes at packaging. After the portlets have been built, you will need to execute the `build.xml` [ant] script to deploy the portlets on [WS-PGRADE].

Create a copy of `deployment.properties.example` and save it as `deployment.properties`. Fill in the pertinent information. Once you're done, make sure the portlets have been built by executing [mvn] as follows:

	$ mvn package

Then execute [ant] like so:

    $ ant deploy
    
This last command will copy the portlets into the `deploy` folder of your [WS-PGRADE] instance. After deployment finishes, don't forget to restart the [WS-PGRADE] portal. 

## A note about certificates
As already discussed, these portlets allow you to access a [UNICORE] instance and query its incarnation database (IDB). As you might know, [UNICORE] relies on the use of certificates. Chances are that if your [WS-PGRADE] instance, on which you will install this portlet, can submit jobs on a [UNICORE] grid, then you're good to go. However, we think there is some value in documenting the followed process in order to connect a [WS-PGRADE] instance to a [UNICORE] grid.

### First things first: getting a server certificate.
The very first thing you need is a way to identify your [WS-PGRADE] server. This is done by using a server certificate. Server certificates are issued by a so-called Certificate Authority, [CA]. This is just a fancy name for an institution that, among other services, can issue digital certificates, which is just a fancy name for a file with numbers in it. This step depends on where you work/live, but at the very least, you will need a Certificate Signing Request [CSR]. You will need a simple-yet-powerful tool called [OpenSSL]. With it you can do all sorts of wonderful things, for instance, create a [CSR]. 

#### Generating a private key.
Before generating a [CSR], you will need to generate a private key for your server. Other parties will never have access to your private key, but they can access your public key (see [https://en.wikipedia.org/wiki/Public-key_cryptography] for more information). This key is the _private_ part of the public-private key encryption process. The following [OpenSSL] command generates a private [RSA] key using `2048` bytes in the `server-key.pem` file:

    $ openssl genrsa -out server-key.pem 2048

#### Generating a [CSR].
The following [OpenSSL] command generates a [CSR] in [PEM] format under the file `mycsr.pem`. Notice how this step requires a private key (`server-key.pem`), which you already generated. You will be prompted to input information about the server, namely, its location, its DNS name, etc. You will also be prompted for a challenge password.  

    $ openssl req -new -key server-key.pem -out csr.pem -outform PEM
    
As suggested in [http://www.shellhacks.com/en/HowTo-Create-CSR-using-OpenSSL-Without-Prompt-Non-Interactive], you can also use the `-subj` command line option to set most of the _certificate's subject_ information without using the prompt (you will still be asked for a password):
 
     $ openssl req -new -key server-key.pem -out csr.pem -outform PEM -subj /C=DE/O=GridGermany/OU=Fake\ University/CN=myserver.edu
     
Your newly generated [CSR], stored in `csr.pem` would look like:

    -----BEGIN CERTIFICATE REQUEST-----
    YADAYADAYADAYADAYADAYADAYADAYADAYADAYADAYADAYADAYADAYADAYADAYADA
    YADAYADAYADAYADAYADAYADAYADAYADAYADAYADAYADAYADAYADAYADAYADAYADA
    ...
    YADAYADAYADAYADAYADAYADAYADAYADAYADAYADAYA
	-----END CERTIFICATE REQUEST-----
     
#### Requesting a server certificate
At this point, you have a private key, `server-key.pem`, and a [CSR], `csr.pem`. The next step is to go to your [CA] and request for a server certificate. Depending on your [CA], you will get your certificate in a different format, but let's assume that you got it in [PEM] format, stored it under `server-cert.pem` and it looks similar to this:

	-----BEGIN CERTIFICATE-----
	NANANANANANANANANANANANANANANANANANANANANANANANANANANANANANANANA
	NANANANANANANANANANANANANANANANANANANANANANANANANANANANANANANANA
	...
	NANANANANANANANANANANANANANANANABATMAN
	-----END CERTIFICATE-----

### Using [UNICORE]'s API
With your newly acquired certificate, you are a step closer to query [UNICORE] grids. There are still a couple of things to do. [UNICORE] requires your server certificate stored in a [PKCS12] file, along with the root certificate of your [CA] in a Java keystore file. Let's see how to achieve this.

#### Exporting your server's certificate to [PKCS12] format
Once your [CA] has issued your server's certificate, use [OpenSSL] to store it in a [PKCS12] file, like so:

	$ openssl pkcs12 -export -out server-store.p12 -inkey server-key.pem -in server-cert.pem -name myalias
	
This will generate a file in [PKCS12] format, in which your server's certificate will be contained. In order to make things _friendlier_, you are allowed to use an _alias_ for your server, in this case, we chose `myalias`. You will be prompted for a password. Let's assume that you chose `server-store-pass` as a password.

#### Exporting your [CA]'s root certificate into a Java keystore
The [UNICORE] API also requires a keystore in which your [CA]'s root certificate has been included. Check your local [CA]'s documentation to obtain the root certificate. Let's assume that you stored in [PEM] format in the file `ca-root.pem` and that it looks similar to:

	-----BEGIN CERTIFICATE-----
	CANTTOUCHTHISCANTTOUCHTHISCANTTOUCHTHISCANTTOUCHTHISCANTTOUCHTHIS
	CANTTOUCHTHISCANTTOUCHTHISCANTTOUCHTHISCANTTOUCHTHISCANTTOUCHTHIS
	...
	CANTTOUCHTHISCANTTOUCHTHISSTOPHAMMERTIME
	-----END CERTIFICATE-----
     
You will need Oracle's [keytool] to generate a Java keystore, which is usually shipped in the Java Software Development Kit (Java SDK). Executing the following command will import your [CA]'s root certificate into a keystore and save it under a file named `truststore.jks`: 
 
	$ keytool -import -keystore truststore.jks -trustcacerts -file ca-root.pem
	
Again, you will be prompted for a password. Let's assume you entered `ca-root-pass`.

### Putting it all together
This is a checklist of the things you need to make your [WS-PGRADE] instance _talk_ to a [UNICORE] grid:

* A file in [PKCS12] format containing your server certificate under a certain alias, along with the password you used for its generation (e.g., `server-store.p12` containing `server-cert.pem` with the `myalias` alias using `server-store-pass` as password)
* A Java keystore file containing your [CA]'s root certificate, along with the password you use for its generation (e.g., `truststore.jks` using `ca-root-pass` as password)
* A running [WS-PGRADE] instance

You now need to configure your [WS-PGRADE] instance so it can interact with a [UNICORE] grid. Navigate to `<wspgrade-instance-url>/dci_bridge_service/conf` and enter your Apache Tomcat admin password. Click on `Grids` and then on `UNICORE`. Add a new [UNICORE] service by clicking on the `Add new` tab. The following table shows the values you should use (note that all paths refer to file paths on your [WS-PGRADE] server):

* UNICORE grid name: the DNS name of a [UNICORE] instance, e.g., `unicore.server.edu:8081`	
* Status: click on `Enabled`
* Path of keystore file for resources(pkcs12): the path, on your [WS-PGRADE] server, of the [PKCS12] file containing your server certificate (e.g., the path of the `server-store.p12` file)	
* Password of resource keystore: the password you used to generate the Java keystore (e.g., `server-store-pass`) 	
* Alias of new resource: the alias you used when importing your server's certificate into the Java keystore (e.g., `myalias`)	
* DN of new resource: the domain name of your server (e.g., `CN=myserver.edu, OU=Fake University, O=GridGermany, C=DE`)	
* Path of keystore file for CA-s(jks): the path of the Java keystore file containing your [CA]'s root certificate (e.g., the path of the `truststore.jks` file)
* Password of CA keystore: the password you use to generate the Java keystore (e.g., `ca-root-pass`)  

After clicking on the `Save` button, click on the `Middleware settings` tab and make sure that the `Enable plugin` option is set to `Enabled`. Click on the `Save` button and after a restart of your [WS-PGRADE] instance you should be ready to go.




[UNICORE]: http://www.unicore.eu
[WS-PGRADE]: http://guse.hu
[CA]: https://en.wikipedia.org/wiki/Certificate_authority
[CSR]: https://en.wikipedia.org/wiki/Certificate_signing_request
[OpenSSL]: https://www-origin.openssl.org/
[RSA]: https://en.wikipedia.org/wiki/RSA_(cryptosystem)
[PKCS12]: https://en.wikipedia.org/wiki/PKCS_12
[PEM]: https://en.wikipedia.org/wiki/Privacy-enhanced_Electronic_Mail
[keytool]: https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html
[maven]: https://maven.apache.org/
[ant]: https://ant.apache.org
[KNIME]: https://knime.org
[KNIME2gUSE]: https://github.com/WorkflowConversion/KNIME2gUSE
[deploy1]: https://github.com/WorkflowConversion/WorkflowConversion.github.io/blob/master/images/portlets/deploy1.png
[deploy2]: https://github.com/WorkflowConversion/WorkflowConversion.github.io/blob/master/images/portlets/deploy2.png
[deploy3]: https://github.com/WorkflowConversion/WorkflowConversion.github.io/blob/master/images/portlets/deploy3.png
[deploy4]: https://github.com/WorkflowConversion/WorkflowConversion.github.io/blob/master/images/portlets/deploy4.png