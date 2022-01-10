# Alterorb Patcher

A CLI application to patch the original FunOrb jars.

### Patches applied

* String decryption & inlining;
* Mouse right click fix for jdk9+;
* RSA Public Key replacement;
* Allow any host for applet document & code bases;

### Command line parameters

| Arg    |     Required     | Description                                                                                                                                                         |
|--------|:----------------:|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| patch  |    yes (task)    | Indicates that the patch games task should be run                                                                                                                   |
| gencfg |    yes (task)    | Indicates that the generate launcher config task should be run                                                                                                      | 
| src    | yes (task/patch) | A source directory containing the jar files                                                                                                                         |
| out    |   yes (patch)    | The output directory                                                                                                                                                |
| pubkey |    no (patch)    | A file or URL containing a x509 encoded RSA public key to be used as replacement, if this is not provided, a keypair is generated and saved to the output directory |

### Original jar sha256 sums

In order to reproduce the jars available from AlterOrb, you'll need the original game jars matching the following sha256 sums:

```
00d93f10c0c917c825f0c3cad3dfeb3f367dc820bdbffd921e89757be9f53f84 36cardtrick.jar
8bd2dd298eb396debdf580fb0e5b779e9b62cd9f8ea992e6370670c8c8d71c14 aceofskies.jar
173d944fdd37bcab91cd18c4b505ab6edec1fbccffb9962979aaa2cbdc2dd5fa arcanistsmulti.jar
262eee8f7ba219ddac8f802e0826f35936a8d95d72f5a7470dd1fcdbf5d50d8f armiesofgielinor.jar
61d6352024c4886191b165a007d6374f164e5c66d1bbe78ed667e1a32cc21285 bachelorfridge.jar
dbba4d2283740c75528b3e243fdd28fed34d8399d690747e50bbc61d27d8470a bouncedown.jar
c47b429a3dd529dc27bb1cc960cb086fcac3e7ff085e4daec13092a1c1dd848f brickabrac.jar
27c7622d4a6453084f39a716e580dbc35bb76f6eb2a3eb6df504f8bffd475314 chess.jar
5cfb7334c1457a61d0e28e7e1db3a32d699bf5daa9473f42ef774e357814cd38 confined.jar
21168f5143369791ff395e826e3634659986eb954d01152705bb31f44d5c1d50 crazycrystals.jar
9a4c40f5848e992dbc82b6e85873f2ed16d65eee2205480b487fd3940715fd49 dekobloko.jar
8a73ae4fccf84a39cab104c39b56047d684519c12465266f99c76d7a9a1a6c5c drphlogistonsavestheearth.jar
fdb184c4f297502987800c127704c37b8323681e38918edae996565064561a86 dungeonassault.jar
aac02085b7086911a4dd30c899620952fbae8a38b8b6286828f81098a82e30e0 escapevector.jar
1f5dfd3dd0d6d6aade0e79bea506069f84f648255f4616db6a09506146197e22 fleacircus.jar
05d41857528c2486b0ee3ccc4a98d7b1c40a7f0012257e46e3f2dbe7046910b9 geoblox.jar
95bf3d8d030bf163c77dc2bdead5fd5aea7c90ce602e4cba966d3f8a0cadc955 holdtheline.jar
fa22a5c26765121a04384d55119d6d619f21da3bbde9d2109d297a13ecdff66c hostilespawn_vengeance.jar
83f914e90ee3923c178d21b82fd7f1c48b4bda70d8771d3e5ed56e124d09846b kickabout.jar
b703b6f19b3cdc826ad3dae1d569bedd466b6cf25d8f5c2950859b10016ada8e lexicominos.jar
0af0a18150fa4ed3a8af1c4ace149f5adedec464e6442d654f266edeb48baf73 minerdisturbance.jar
f81efb9352d7182257bde66557c1d00e1e4cd122b4ff082b65efc77ddcae51da monkeypuzzle2.jar
335497314b48c7563ce443bbf1245dda7ba3536f13bfb072079f7199fa4bcb49 orbdefence.jar
7582b4f8aaa862375211691cbdb00f0122a71b8834c49e7e13daf992211e14aa pixelate.jar
d812064beb8e9e2241d7fa30f26e714737624dedede5560fa3d4e5357d75eced pool.jar
02cd0e44e78959e0318d18d51772e98401a6049dca3de4150fecc62a84d0b90b shatteredplans.jar
b8f7e2a3885aa3073897dafe9902555853993bf4caeeb85c14c72ffd7be6d259 solknight.jar
24204773e8555501287b5fe4e6d8deb04e5ee10a474403c762a866ba0fc903f5 starcannon.jar
6f756d63c837c90762d648a730c87fe90593516fb3ef3f968a5d862cfd43bf04 steelsentinels.jar
c19eb0501bf125ae911604fe083830f191ff55122a0af3e602e367cded6f99af stellarshard.jar
167758ae8879d20e1d58f067716e014ad1ee7049ba39fb621958b0fe97af77e7 sumoblitz.jar
e76934f58bb49241d31a959a5217c3627955cc551f410a4fcea0db2f9962bf6c terraphoenix.jar
f387e40b281940f8954ee52b912ac51221253203bbc64dc078658cab4d5b4f32 tetralink.jar
f79303f59a030d24243a67571ff0a2b876fd908a084fdd23087eaaead0e94d9b tombracer.jar
d5083ccc2dbeb0552b347bf0de1b95f87861f7c77f97b176978e2256be4f00ae torchallenge.jar
e95dfe9353985d7b93f3f8ccb6209abe5b2e8a460f0f9c1d14e9e90862e4e7e3 torquing.jar
613e781dfd17a2a207fff2a0c4aba7cde87a5a5bc839184254a7a07b4121da82 trackcontroller.jar
a1e96b157dcc8c9bca3c449ee7835e68f77cd279729152618f9e22648c14c815 transmogrify.jar
72ad41c657e3b2a27b769ef25d01e04b9a84a2cba011f2eb05937e1f6a4359e9 vertigo2.jar
01006d5ec037039a7e8970497a48cce75b2d55bc40dda1ab742defb82ba04098 virogrid.jar
625ca14993df1ea48d20c4c23286ae2cf0f2129302f88fae4d1b331cb336c220 voidhunters.jar
1890dd89afc260a227390c3971da25625215a33c08dd4e5f3724d87da4491f48 wizardrun.jar
1ea9b1bcc9f4e2474a160d4e43dc73edb083942b1bf2c81cf8f66c01d1b1455f zombiedawn.jar
728378b59694aec182a96eda6f5878cf853b466aab05208fce6ae0f526b9c789 zombiedawnmulti.jar
```

### Patching the games:

```
java -jar patcher.jar --patch --src funorb-jars --out funorb-jars/patched --pubkey https://static.alterorb.net/launcher/v3/rsa-public.key 
```