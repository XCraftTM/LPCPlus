package me.wikmor.lpcplus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LPCPlusTest {

	@Test
	void translatesSectionSignLegacyCodes() {
		assertEquals("<green>Green <bold>Bold",
				LPCPlus.translateLegacyToMiniMessage("§aGreen §lBold"));
	}

	@Test
	void translatesSectionSignHexColors() {
		assertEquals("<#12abcd>Hex",
				LPCPlus.translateLegacyToMiniMessage("§x§1§2§a§b§c§dHex"));
	}

	@Test
	void translatesUppercaseSectionSignHexColors() {
		assertEquals("<#ABCDEF>Hex",
				LPCPlus.translateLegacyToMiniMessage("§X§A§B§C§D§E§FHex"));
	}

	@Test
	void translatesMixedLegacyPrefixes() {
		assertEquals("<green>Green <red>Red",
				LPCPlus.translateLegacyToMiniMessage("&aGreen §cRed"));
	}

	@Test
	void leavesIncompleteSectionSignHexPrefixUntranslated() {
		assertEquals("§x<dark_blue><dark_green>",
				LPCPlus.translateLegacyToMiniMessage("§x§1§2"));
	}

	@Test
	void translatesAmpersandHexColors() {
		assertEquals("<#a1B2c3>Hex",
				LPCPlus.translateLegacyToMiniMessage("&#a1B2c3Hex"));
	}
}
