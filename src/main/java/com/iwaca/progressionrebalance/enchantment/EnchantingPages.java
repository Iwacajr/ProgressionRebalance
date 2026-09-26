package com.iwaca.progressionrebalance.enchantment;

/**
 * Lapis page arithmetic for the enchanting table.
 *
 * <p>The lapis in the table selects a page: 1-3 lapis is page 0, 4-6 is page 1, 7-9 is page 2, and so on up
 * to the configured maximum. Each page is a separate deterministic set of three offers (derived from the
 * player's enchanting seed), and offer {@code slot} on page {@code p} costs {@code 3p + slot + 1} lapis.
 * Page 0 is exactly vanilla, including its seed, so the first page shows the offers vanilla would show.
 */
public final class EnchantingPages {
    public static final int OFFERS_PER_PAGE = 3;

    /** Odd 64-bit constant (golden ratio) that spreads page seeds far away from vanilla's seed + slot. */
    private static final long PAGE_SEED_STRIDE = 0x9E3779B97F4A7C15L;

    private EnchantingPages() {
    }

    public static int pageFor(int lapisCount, int maxPages) {
        if (lapisCount <= 0 || maxPages <= 1) {
            return 0;
        }
        return Math.min((lapisCount - 1) / OFFERS_PER_PAGE, maxPages - 1);
    }

    public static int lapisCost(int page, int slot) {
        return page * OFFERS_PER_PAGE + slot + 1;
    }

    public static long pageSeed(long vanillaSeed, int page) {
        return vanillaSeed + page * PAGE_SEED_STRIDE;
    }
}
