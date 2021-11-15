package net.alterorb.patcher;

public enum FunOrbGame {

    TIRTHY_SIX_CARD_TRICK("36cardtrick", "Thirty-Six Card Trick"),
    CHESS("chess", "Chess"),
    KICKABOUT("kickabout", "Kickabout League"),
    PIXELATE("pixelate", "Pixelate"),
    ACE_OF_SKIES("aceofskies", "Ace of Skies"),
    ARCANISTS("arcanistsmulti", "Arcanists"),
    ARMIES_OF_GIELINOR("armiesofgielinor", "Armies of Gielinor"),
    BACHELOR_FRIDGE("bachelorfridge", "Bachelor Fridge"),
    BOUNCEDOWN("bouncedown", "Bouncedown"),
    BRICK_A_BRACK("brickabrac", "Brick-à-Brac"),
    CONFINED("confined", "Confined"),
    CRAZY_CRYSTALS("crazycrystals", "Crazy Crystals"),
    DEKOBLOKO("dekobloko", "Deko Bloko"),
    DR_PHLOGISTON_SAVES_THE_EARTH("drphlogistonsavestheearth", "Dr. Phlogiston Saves The Earth"),
    DUNGEON_ASSAULT("dungeonassault", "Dungeon Assault"),
    ESCAPE_VECTOR("escapevector", "Escape Vector"),
    FLEA_CIRCUS("fleacircus", "Flea Circus"),
    GEOBLOX("geoblox", "Geoblox"),
    HOLD_THE_LINE("holdtheline", "Hold The Line"),
    HOSTILE_SPAWN("hostilespawn_vengeance", "Hostile Spawn"),
    LEXICOMINOS("lexicominos", "Lexicominos"),
    MINER_DISTURBANCE("minerdisturbance", "Miner Disturbance"),
    MONKEY_PUZZLE_2("monkeypuzzle2", "Monkey Puzzle 2"),
    ORB_DEFENCE("orbdefence", "Orb Defence"),
    POOL("pool", "Pool"),
    SHATTERED_PLANS("shatteredplans", "Shattered Plans"),
    SOL_KNIGHT("solknight", "Sol-Knight"),
    STAR_CANNON("starcannon", "Star Cannon"),
    STEEL_SENTINELS("steelsentinels", "Steel Sentinels"),
    STELLAR_sHARD("stellarshard", "Stellar Shard"),
    SUMOBLITZ("sumoblitz", "Sumoblitz"),
    TERRAPHOENIX("terraphoenix", "TerraPhoenix"),
    TETRALINK("tetralink", "TetraLink"),
    TOMB_RACER("tombracer", "Tomb Racer"),
    TOR_CHALLENGE("torchallenge", "Tor Challenge"),
    TORQUING("torquing", "Torquing!"),
    TRACK_CONTROLLER("trackcontroller", "The Track Controller"),
    TRANSMOGRIFY("transmogrify", "Transmogrify"),
    VERTIGO_2("vertigo2", "Vertigo 2"),
    VIROGRID("virogrid", "Virogrid"),
    VOID_HUNTERS("voidhunters", "Void Hunters"),
    WIZARD_RUN("wizardrun", "Wizard Run"),
    ZOMBIE_DAWN("zombiedawn", "Zombie Dawn"),
    ZOMBIE_DAWN_MULTI("zombiedawnmulti", "Zombie Dawn Multiplayer");

    private final String internalName;
    private final String fancyName;

    FunOrbGame(String internalName, String fancyName) {
        this.internalName = internalName;
        this.fancyName = fancyName;
    }

    public String internalName() {
        return internalName;
    }

    public String fancyName() {
        return fancyName;
    }
}
