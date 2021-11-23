package net.alterorb.patcher;

public enum FunOrbGame {

    ACE_OF_SKIES("aceofskies", "Ace of Skies", "AceOfSkies"),
    ARCANISTS("arcanistsmulti", "Arcanists", "ArcanistsMulti"),
    ARMIES_OF_GIELINOR("armiesofgielinor", "Armies of Gielinor", "ArmiesOfGielinor"),
    BACHELOR_FRIDGE("bachelorfridge", "Bachelor Fridge", "BachelorFridge"),
    BOUNCEDOWN("bouncedown", "Bouncedown", "Bounce"),
    BRICK_A_BRACK("brickabrac", "Brick-à-Brac", "BrickABrac"),
    CHESS("chess", "Chess", "Chess"),
    CONFINED("confined", "Confined", "Confined"),
    CRAZY_CRYSTALS("crazycrystals", "Crazy Crystals", "CrazyCrystals"),
    DEKOBLOKO("dekobloko", "Deko Bloko", "client"),
    DR_PHLOGISTON_SAVES_THE_EARTH("drphlogistonsavestheearth", "Dr. Phlogiston Saves The Earth", "DrPhlogistonSavesTheEarth"),
    DUNGEON_ASSAULT("dungeonassault", "Dungeon Assault", "DungeonAssault"),
    ESCAPE_VECTOR("escapevector", "Escape Vector", "EscapeVector"),
    FLEA_CIRCUS("fleacircus", "Flea Circus", "fleas"),
    GEOBLOX("geoblox", "Geoblox", "Geoblox"),
    HOLD_THE_LINE("holdtheline", "Hold The Line", "HoldTheLine"),
    HOSTILE_SPAWN("hostilespawn_vengeance", "Hostile Spawn", "HostileSpawn"),
    KICKABOUT("kickabout", "Kickabout League", "Kickabout"),
    LEXICOMINOS("lexicominos", "Lexicominos", "Lexicominos"),
    MINER_DISTURBANCE("minerdisturbance", "Miner Disturbance", "MinerDisturbance"),
    MONKEY_PUZZLE_2("monkeypuzzle2", "Monkey Puzzle 2", "MonkeyPuzzle2"),
    ORB_DEFENCE("orbdefence", "Orb Defence", "OrbDefence"),
    PIXELATE("pixelate", "Pixelate", "Pixelate"),
    POOL("pool", "Pool", "Pool"),
    SHATTERED_PLANS("shatteredplans", "Shattered Plans", "ShatteredPlansClient"),
    SOL_KNIGHT("solknight", "Sol-Knight", "SolKnight"),
    STAR_CANNON("starcannon", "Star Cannon", "StarCannon"),
    STEEL_SENTINELS("steelsentinels", "Steel Sentinels", "SteelSentinels"),
    STELLAR_SHARD("stellarshard", "Stellar Shard", "stellarshard"),
    SUMOBLITZ("sumoblitz", "Sumoblitz", "Sumoblitz"),
    TERRAPHOENIX("terraphoenix", "TerraPhoenix", "Terraphoenix"),
    TETRALINK("tetralink", "TetraLink", "TetraLink"),
    TIRTHY_SIX_CARD_TRICK("36cardtrick", "Thirty-Six Card Trick", "Main"),
    TOMB_RACER("tombracer", "Tomb Racer", "TombRacer"),
    TOR_CHALLENGE("torchallenge", "Tor Challenge", "TorChallenge"),
    TORQUING("torquing", "Torquing!", "Torquing"),
    TRACK_CONTROLLER("trackcontroller", "The Track Controller", "TrackController"),
    TRANSMOGRIFY("transmogrify", "Transmogrify", "Transmogrify"),
    VERTIGO_2("vertigo2", "Vertigo 2", "Vertigo2"),
    VIROGRID("virogrid", "Virogrid", "Virogrid"),
    VOID_HUNTERS("voidhunters", "Void Hunters", "VoidHunters"),
    WIZARD_RUN("wizardrun", "Wizard Run", "wizardrun"),
    ZOMBIE_DAWN("zombiedawn", "Zombie Dawn", "ZombieDawn"),
    ZOMBIE_DAWN_MULTI("zombiedawnmulti", "Zombie Dawn Multiplayer", "ZombieDawnMulti");

    private final String internalName;
    private final String fancyName;
    private final String mainClass;

    FunOrbGame(String internalName, String fancyName, String mainClass) {
        this.internalName = internalName;
        this.fancyName = fancyName;
        this.mainClass = mainClass;
    }

    public String internalName() {
        return internalName;
    }

    public String fancyName() {
        return fancyName;
    }

    public String mainClass() {
        return mainClass;
    }
}
