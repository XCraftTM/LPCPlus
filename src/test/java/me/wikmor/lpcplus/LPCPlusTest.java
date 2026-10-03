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
}
