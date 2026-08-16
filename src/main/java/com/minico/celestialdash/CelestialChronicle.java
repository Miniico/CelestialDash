package com.minico.celestialdash;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Creates the one-time written chronicle that introduces CelestialDash lore.
 */
final class CelestialChronicle {

    private static final String AUTHOR = "Dr. Elian Voss";
    private static final String GLINT_OVERRIDE_METHOD = "setEnchantmentGlint" + "Override";
    // Resolving this optional API once avoids scanning ItemMeta methods for every chronicle.
    // A null value deliberately preserves support for servers that predate the override.
    private static final Method GLINT_OVERRIDE = findGlintOverride();
    private static final int MAX_CHARACTERS_PER_LINE = 16;
    private static final int MAX_LINES_PER_PAGE = 11;

    private CelestialChronicle() {
    }

    @SuppressWarnings("deprecation") // Uses legacy String pages for the oldest supported server API.
    static ItemStack create(String clientLocale, NamespacedKey chronicleKey) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta itemMeta = book.getItemMeta();
        if (!(itemMeta instanceof BookMeta meta)) {
            return book;
        }

        ChronicleText text = ChronicleLanguage.fromClientLocale(clientLocale).text;
        meta.setTitle(text.title());
        meta.setAuthor(AUTHOR);
        meta.setPages(text.pages());
        meta.getPersistentDataContainer().set(chronicleKey, PersistentDataType.BYTE, (byte) 1);
        applyVisualGlint(meta);
        book.setItemMeta(meta);
        return book;
    }

    static String deliveryMessage(String clientLocale) {
        return ChronicleLanguage.fromClientLocale(clientLocale).text.deliveryMessage();
    }

    private enum ChronicleLanguage {
        ENGLISH(
                "The Falling Sky",
                "You received The Falling Sky.",
                """
                        When the old observatory heard the sky fracture,
                        we thought the stars were dying.

                        Instead, fragments crossed the void and fell beyond
                        our walls: cold, bright, and weeping silver light.
                        The miners named them Celestial Tears.

                        They were not stone. They remembered heat, fear,
                        and the last breath of distant suns.
                        """,
                """
                        I set four Tears around a core of Netherite,
                        the only metal stubborn enough to hold their song.

                        By morning, an Amulet rested on the bench.

                        Worn close to the heart, it draws poison, weakness,
                        and stranger afflictions into its fading light.
                        Even flame forgets how to burn before it.

                        If more stars fall, keep one close.
                        """
        ),
        SPANISH(
                "El cielo que cae",
                "Has recibido El cielo que cae.",
                """
                        Cuando el viejo observatorio oyó quebrarse el cielo,
                        creímos que las estrellas morían.

                        Pero fragmentos cruzaron el vacío y cayeron más allá
                        de nuestros muros: fríos, brillantes, llorando luz plateada.
                        Los mineros los llamaron Lágrimas Celestiales.

                        No eran piedra. Guardaban el calor, el miedo
                        y el último aliento de soles lejanos.
                        """,
                """
                        Coloqué cuatro Lágrimas alrededor de un núcleo de Netherite,
                        el único metal bastante obstinado para contener su canto.

                        Al amanecer, un Amuleto descansaba sobre la mesa.

                        Llevado junto al corazón, atrae veneno, debilidad
                        y males más extraños hacia su luz menguante.
                        Hasta el fuego olvida cómo arder ante él.

                        Si caen más estrellas, guarda una cerca.
                        """
        ),
        PORTUGUESE(
                "O Céu que Cai",
                "Você recebeu O Céu que Cai.",
                """
                        Quando o velho observatório ouviu o céu se partir,
                        pensámos que as estrelas morriam.

                        Em vez disso, fragmentos atravessaram o vazio e caíram além
                        dos nossos muros: frios, luminosos, vertendo luz prateada.
                        Os mineiros deram-lhes o nome de Lágrimas Celestiais.

                        Não eram pedra. Lembravam-se do calor, do medo
                        e do último suspiro de sóis distantes.
                        """,
                """
                        Coloquei quatro Lágrimas em volta de um núcleo de Netherite,
                        o único metal teimoso o bastante para guardar o seu canto.

                        Ao amanhecer, um Amuleto repousava sobre a bancada.

                        Usado junto ao coração, puxa veneno, fraqueza
                        e aflições mais estranhas para a sua luz que se apaga.
                        Até as chamas esquecem como queimar diante dele.

                        Se mais estrelas caírem, guarde uma por perto.
                        """
        ),
        ITALIAN(
                "Il cielo che cade",
                "Hai ricevuto Il cielo che cade.",
                """
                        Quando il vecchio osservatorio udì il cielo spezzarsi,
                        pensammo che le stelle stessero morendo.

                        Invece, frammenti attraversarono il vuoto e caddero oltre
                        le nostre mura: freddi, luminosi, stillando luce argentea.
                        I minatori li chiamarono Lacrime Celesti.

                        Non erano pietra. Ricordavano il calore, la paura
                        e l'ultimo respiro di soli lontani.
                        """,
                """
                        Disposi quattro Lacrime attorno a un nucleo di Netherite,
                        l'unico metallo abbastanza ostinato da custodire il loro canto.

                        Al mattino, un Amuleto riposava sul banco.

                        Portato vicino al cuore, attira veleno, debolezza
                        e afflizioni più strane nella sua luce che svanisce.
                        Persino le fiamme dimenticano come bruciare davanti a lui.

                        Se cadranno altre stelle, tienine una vicino.
                        """
        ),
        FRENCH(
                "Le ciel qui tombe",
                "Vous avez reçu Le ciel qui tombe.",
                """
                        Quand le vieil observatoire entendit le ciel se briser,
                        nous avons cru que les étoiles mouraient.

                        Pourtant, des fragments traversèrent le vide et tombèrent au-delà
                        de nos murs: froids, lumineux, pleurant une lumière d'argent.
                        Les mineurs les nommèrent Larmes Célestes.

                        Ils n'étaient pas de la pierre. Ils se souvenaient de la chaleur,
                        de la peur et du dernier souffle de soleils lointains.
                        """,
                """
                        J'ai posé quatre Larmes autour d'un cœur de Netherite,
                        le seul métal assez obstiné pour retenir leur chant.

                        À l'aube, une Amulette reposait sur l'établi.

                        Portée près du cœur, elle attire poison, faiblesse
                        et des maux plus étranges dans sa lumière déclinante.
                        Même le feu oublie comment brûler devant elle.

                        Si d'autres étoiles tombent, gardez-en une près de vous.
                        """
        ),
        RUSSIAN(
                "Падающее небо",
                "Вы получили «Падающее небо».",
                """
                        Когда старая обсерватория услышала, как треснуло небо,
                        мы решили, что звёзды умирают.

                        Но осколки пересекли пустоту и упали за нашими стенами:
                        холодные, сияющие, проливая серебряный свет.
                        Шахтёры назвали их Небесными Слёзами.

                        Они не были камнем. Они помнили тепло, страх
                        и последний вздох далёких солнц.
                        """,
                """
                        Я положил четыре Слезы вокруг ядра из Незерита,
                        единственного металла, достаточно упрямого, чтобы удержать их песнь.

                        К утру на верстаке лежал Амулет.

                        У сердца он втягивает яд, слабость
                        и более странные недуги в свой гаснущий свет.
                        Даже огонь забывает, как гореть перед ним.

                        Если упадут новые звёзды, держите одну рядом.
                        """
        );

        private final ChronicleText text;

        ChronicleLanguage(String title, String deliveryMessage, String firstPage, String secondPage) {
            text = new ChronicleText(title, deliveryMessage, paginate(firstPage + "\n\n" + secondPage));
        }

        private static ChronicleLanguage fromClientLocale(String clientLocale) {
            if (clientLocale == null || clientLocale.isBlank()) {
                return ENGLISH;
            }

            String normalized = clientLocale.toLowerCase(Locale.ROOT).replace('-', '_');
            int separator = normalized.indexOf('_');
            String language = separator >= 0 ? normalized.substring(0, separator) : normalized;
            return switch (language) {
                case "es" -> SPANISH;
                case "pt" -> PORTUGUESE;
                case "it" -> ITALIAN;
                case "fr" -> FRENCH;
                case "ru" -> RUSSIAN;
                default -> ENGLISH;
            };
        }
    }

    private static List<String> paginate(String text) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.strip().split("\\R\\s*\\R")) {
            if (!lines.isEmpty()) {
                lines.add("");
            }
            lines.addAll(wrapParagraph(paragraph));
        }

        List<String> pages = new ArrayList<>();
        List<String> currentPage = new ArrayList<>();
        for (String line : lines) {
            if (currentPage.isEmpty() && line.isEmpty()) {
                continue;
            }
            if (currentPage.size() == MAX_LINES_PER_PAGE) {
                pages.add(String.join("\n", currentPage));
                currentPage.clear();
            }
            currentPage.add(line);
        }
        if (!currentPage.isEmpty()) {
            pages.add(String.join("\n", currentPage));
        }
        return List.copyOf(pages);
    }

    private static List<String> wrapParagraph(String paragraph) {
        String normalized = paragraph.replaceAll("\\s+", " ").trim();
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : normalized.split(" ")) {
            if (word.length() > MAX_CHARACTERS_PER_LINE) {
                appendLine(lines, line);
                for (int start = 0; start < word.length(); start += MAX_CHARACTERS_PER_LINE) {
                    int end = Math.min(start + MAX_CHARACTERS_PER_LINE, word.length());
                    lines.add(word.substring(start, end));
                }
                continue;
            }

            if (line.isEmpty()) {
                line.append(word);
            } else if (line.length() + 1 + word.length() <= MAX_CHARACTERS_PER_LINE) {
                line.append(' ').append(word);
            } else {
                appendLine(lines, line);
                line.append(word);
            }
        }
        appendLine(lines, line);
        return lines;
    }

    private static void appendLine(List<String> lines, StringBuilder line) {
        if (!line.isEmpty()) {
            lines.add(line.toString());
            line.setLength(0);
        }
    }

    private record ChronicleText(String title, String deliveryMessage, List<String> pages) {
    }

    /**
     * Adds only the native visual glint when the running server exposes its API.
     * Older 1.20 servers do not offer a non-enchantment glint override, so they
     * receive an ordinary written book rather than a fake enchantment.
     */
    private static void applyVisualGlint(ItemMeta meta) {
        if (GLINT_OVERRIDE == null) {
            return;
        }

        try {
            GLINT_OVERRIDE.invoke(meta, Boolean.TRUE);
        } catch (ReflectiveOperationException ignored) {
            // The chronicle remains usable if a server cannot invoke the optional override.
        }
    }

    private static Method findGlintOverride() {
        for (Method method : ItemMeta.class.getMethods()) {
            if (!method.getName().equals(GLINT_OVERRIDE_METHOD)
                    || method.getParameterCount() != 1
                    || method.getParameterTypes()[0] != Boolean.class) {
                continue;
            }

            return method;
        }
        return null;
    }
}
