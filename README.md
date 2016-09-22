# WS-PGRADE Workflow Conversion Portlets
## What are these portlets?
### The `WorkflowImporter` portlet
This portlet lets users to import workflows created in [KNIME]. 

Imagine a user that has completed a test workflow in [KNIME] and wants to export it to a [WS-PGRADE] instance by using the [KNIME2gUSE] extension. Let's see a simple example to fully understand the problem that this portlet tries to solve.

A scientist has designed a simple [KNIME] workflow, comprised of two applications: _App1_ and _App2_. Our scientist would like to execute her workflow on a local cluster visible on a [WS-PGRADE] instance. On the scientists computer both applications are located on `/usr/bin`, but this is not necessarily true for the clusters available in the [WS-PGRADE] instance. In fact, _App1_ is accessible only to _cluster1_, while _App2_ is accessible only through _cluster2_.

Our scientist could very well design the same workflow again in [WS-PGRADE], but this is where `WorfklowImporter` comes to save the day, since she would take the exported workflow that was generaded by the [KNIME2gUSE] extension and upload it via the `WorkflowImporter` portlet.

The portlet would then notice that there are two applications, _App1_ and _App2_ being used in the imported workflow and will then display the available applications. Our scientist would select the appropriate version of _App1_ and _App2_, finalize the import and then be able to execute the workflow on a distributed environment, _as if_ she had designed the workflow directly in [WS-PGRADE].

### The `ApplicationManager` portlet
[WS-PGRADE] is able to communicate with several resource managers, and these managers often lack an application database of sorts. [UNICORE] excels in this aspect and keeps all applications registered in an _incarnation database_, but this seems to be the exception.

This portlet lets the administrator of the [WS-PGRADE] portal to register applications, thus, extending [WS-PGRADE] by adding its own _application database_.


## How can I build the portlets?
You will need [maven] to build the portlets. After you've installed maven, run the following command to build the portlets:

    $ mvn package

## How can I install the portlets?
There are two ways in which you can deploy the portlets on a [WS-PGRADE] instance:

### Manually deploying the portlets
Once you've built the portlets, `ApplicationManager.war`, `WorkflowImporter.war`, you will need to log-in to your [WS-PGRADE] instance as an administrator, then, go through the following steps:

* Access the control panel ![Alt "Control Panel"][deploy1]
* Locate the _App Manager_ in the control panel bar: ![Alt "Locating the App Manager"][deploy2]
* Click on _Install_: ![Alt "Clicking on Install in the App Manager"][deploy3]
* Select _File upload_ and then click on the _Choose file_ button: ![Alt "Uploading a portlet"][deploy4]
* Navigate to the folder on which the portlets were built (typically, under the `target` folder), select one of the portlets and click on the _Install_ button. You will need to repeat this step for each portlet.

### Deploy the portlets as part of the build process
[maven] can deploy the portlets for you as part of the build process. You will need to create a file, `deployment.properties` (use `deployment.properties.example` as a guide) and then execute [maven] like so:

    $ mvn install
    
This command will build the portlets and deploy them for you by copying them into the `deploy` folder of your [WS-PGRADE] instance. 

## A note about certificates
As already discussed, this portlet allows you to access a [UNICORE] instance and query its incarnation database (IDB). As you might know, [UNICORE] relies on the use of certificates. Chances are that if your [WS-PGRADE] instance, on which you will install this portlet, can submit jobs on a [UNICORE] grid, then you're good to go. However, we think there is some value in documenting the followed process in order to connect a [WS-PGRADE] instance to a [UNICORE] grid.

### First things first: getting a server certificate.
The very first thing you need is a way to identify your [WS-PGRADE] server. This is done by using a server certificate. Server certificates are issued by a so-called Certificate Authority, [CA]. This is just a fancy name for an institution that, among other services, can issue digital certificates, which is just a fancy name for a file with numbers in it. This step depends on where you work/live, but at the very least, you will need a Certificate Signing Request [CSR]. You will need a simple-yet-powerful tool called [OpenSSL]. With it you can do all sorts of wonderful things, for instance, create a [CSR]. 

#### Generating a private key.
Before generating a [CSR], you will need to generate a private key for your server. Other parties will never have access to your private key, but they can access your public key (see [https://en.wikipedia.org/wiki/Public-key_cryptography] for more information). This key is the _private_ part of the public-private key encription process. The following [OpenSSL] command generates a private [RSA] key using `2048` bytes in the `server-key.pem` file:

    $ openssl genrsa -out server-key.pem 2048

#### Generating a [CSR].
The following [OpenSSL] command generates a [CSR] in [PEM] format under the file `mycsr.pem`. Please note that this step requires a private key (`server-key.pem`), which you already generated. You will be prompted to input information about the server, namely, its location, its DNS name, etc. You will also be prompted for a challenge password.  

    $ openssl req -new -key server-key.pem -out csr.pem -outform PEM
    
As suggested in [http://www.shellhacks.com/en/HowTo-Create-CSR-using-OpenSSL-Without-Prompt-Non-Interactive], you can also use the `-subj` command line option to set most of the _certificate's subject_ information without using the prompt (you will still be asked for a password):
 
     $ openssl req -new -key server-key.pem -out csr.pem -outform PEM -subj /C=DE/O=GridGermany/OU=Freiburg\ University/CN=myserver.edu
     
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

+ A file in [PKCS12] format containing your server certificate under a certain alias, along with the password you used for its generation (e.g., `server-store.p12` containing `server-cert.pem` with the `myalias` alias using `server-store-pass` as password)
+ A Java keystore file containing your [CA]'s root certificate, along with the password you use for its generation (e.g., `truststore.jks` using `ca-root-pass` as password)
+ A running [WS-PGRADE] instance

You now need to configure your [WS-PGRADE] instance so it can interact with a [UNICORE] grid. Navigate to `<wspgrade-instance-url>/dci_bridge_service/conf` and enter your Apache Tomcat admin password. Click on `Grids` and then on `UNICORE`. Add a new [UNICORE] service by clicking on the `Add new` tab. The following table shows the values you should use (note that all paths refer to file paths on your [WS-PGRADE] server):

+ UNICORE grid name: the DNS name of a [UNICORE] instance, e.g., `unicore.server.edu:8081`	
+ Status: click on `Enabled`
+ Path of keystore file for resources(pkcs12): the path, on your [WS-PGRADE] server, of the [PKCS12] file containing your server certificate (e.g., the path of the `server-store.p12` file)	
+ Password of resource keystore: the password you use to generate the Java keystore (e.g., `server-store-pass`) 	
+ Alias of new resource: the alias you used when importing your server's certificate into the Java keystore (e.g., `myalias`)	
+ DN of new resource: the domain name of your server (e.g., `CN=myserver.edu, OU=Freiburg University, O=GridGermany, C=DE`)	
+ Path of keystore file for CA-s(jks): the path of the Java keystore file containing your [CA]'s root certificate (e.g., the path of the `truststore.jks` file)
+ Password of CA keystore: the password you use to generate the Java keystore (e.g., `ca-root-pass`)  

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
[KNIME]: https://knime.org
[KNIME2gUSE]: https://github.com/WorkflowConversion/KNIME2gUSE
[deploy1]: https://github.com/WorkflowConversion/WorkflowConversion.github.io/blob/master/images/portlets/deploy1.png
[deploy2]: https://github.com/WorkflowConversion/WorkflowConversion.github.io/blob/master/images/portlets/deploy2.png
[deploy3]: https://github.com/WorkflowConversion/WorkflowConversion.github.io/blob/master/images/portlets/deploy3.png
[deploy4]: https://github.com/WorkflowConversion/WorkflowConversion.github.io/blob/master/images/portlets/deploy4.png