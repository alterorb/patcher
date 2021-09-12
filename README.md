# Alterorb Patcher

A CLI application to patch the original FunOrb jars.

### Patches applied

* String decryption & inlining;
* Mouse right click fix for jdk9+;
* RSA Public Key replacement;
* Allow any host for applet document & code bases;

### Command line parameters

| Arg | Required | Description |
| --- | :---: | --- |
| src | yes | A source directory containing the original jar files |
| out | yes | The output directory |
| pubkey | no | A file containing a x509 encoded RSA public key to be used as replacement, if this is not provided, a keypair is generated and saved to the output directory |
