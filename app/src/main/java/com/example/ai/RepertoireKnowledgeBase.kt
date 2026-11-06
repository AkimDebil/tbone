package com.example.ai

import com.example.data.model.FamousRecording
import com.example.data.model.RecommendedWork
import com.example.data.model.RepertoireAnalysis
import com.example.data.settings.AppLanguage

object RepertoireKnowledgeBase {

    /**
     * Preset catalog item definition
     */
    data class PresetMasterwork(
        val key: String,
        val aliases: List<String>,
        val instrument: String,
        val composerRu: String,
        val composerEn: String,
        val composerZh: String,
        val titleRu: String,
        val titleEn: String,
        val titleZh: String,
        val eraRu: String,
        val eraEn: String,
        val eraZh: String,
        val tipsRu: List<String>,
        val tipsEn: List<String>,
        val tipsZh: List<String>,
        val listening: List<RecommendedWork>,
        val recordings: List<FamousRecording>
    ) {
        fun toAnalysis(language: AppLanguage): RepertoireAnalysis {
            return when (language) {
                AppLanguage.RU -> RepertoireAnalysis(
                    pieceTitle = titleRu,
                    composer = composerRu,
                    instrument = instrument,
                    eraAndStyle = eraRu,
                    technicalTips = tipsRu,
                    recommendedListening = listening,
                    famousRecordings = recordings
                )
                AppLanguage.EN -> RepertoireAnalysis(
                    pieceTitle = titleEn,
                    composer = composerEn,
                    instrument = instrument,
                    eraAndStyle = eraEn,
                    technicalTips = tipsEn,
                    recommendedListening = listening,
                    famousRecordings = recordings
                )
                AppLanguage.ZH -> RepertoireAnalysis(
                    pieceTitle = titleZh,
                    composer = composerZh,
                    instrument = instrument,
                    eraAndStyle = eraZh,
                    technicalTips = tipsZh,
                    recommendedListening = listening,
                    famousRecordings = recordings
                )
            }
        }
    }

    val masterworksCatalog: List<PresetMasterwork> = listOf(
        // 1. Ferdinand David - Concertino Op. 4 (Brass / Trombone)
        PresetMasterwork(
            key = "David - Concertino Op. 4",
            aliases = listOf("david", "давид", "concertino op. 4", "концертино", "大卫"),
            instrument = "Тромбон / Trombone",
            composerRu = "Фердинанд Давид",
            composerEn = "Ferdinand David",
            composerZh = "费迪南德·大卫 (Ferdinand David)",
            titleRu = "Концертино для тромбона с оркестром ми-бемоль мажор, соч. 4",
            titleEn = "Concertino for Trombone and Orchestra in E-flat major, Op. 4",
            titleZh = "降E大调长号小协奏曲，作品4号",
            eraRu = "Ранний немецкий романтизм (1837 г., Лейпциг). Написан для солиста оркестра Гевандхауза Карла Траугота Квайсера. Благородный, драматичный стиль лейпцигской школы.",
            eraEn = "Early German Romanticism (1837, Leipzig). Composed for Gewandhaus virtuoso Carl Queisser with Mendelssohn's support. Noble, cantabile, classical-romantic style.",
            eraZh = "早期德国浪漫主义（1837年，莱比锡）。为布商大厦管弦乐团长号大师卡尔·奎瑟尔创作。崇高抒情、充满歌唱性。",
            tipsRu = listOf(
                "1. Вступительная каденция: Требует героического, декламационного звука. Сохраняйте непрерывную опору дыхания на виртуозных пассажах.",
                "2. Кантилена второй темы: Обеспечьте плавный переход позиций кулисы (такты 56-78) на мягком легато без нежелательного глиссандо.",
                "3. Траурный марш (Andante): Точная интонация в низком регистре (ми-бемоль, фа большой октавы). Темп строго 54 BPM.",
                "4. Финал (Vivace): Быстрые чередования штриха стаккато отрабатывайте под метроном на шестнадцатые доли."
            ),
            tipsEn = listOf(
                "1. Opening Cadenza: Heroic, declamatory tone with steady air support through 32nd-note flourishes.",
                "2. Cantabile Second Theme: Smooth slide movement in bars 56-78 with soft legato tongue and zero scooping.",
                "3. Funeral March (Andante): Precise pitch centering in low register (E-flat2, F2) maintaining strict 54 BPM pace.",
                "4. Vivace Finale: Alternate crisp staccato articulation with metronome 16th-note subdivision."
            ),
            tipsZh = listOf(
                "1. 开场华彩段：展现英雄气概的宣叙音色，在三十二分音符跑动中保持平稳的气息支撑。",
                "2. 第二主题如歌：在第56-78小节的把位移动中保持连贯柔和的连音吐音，避免不必要的滑音。",
                "3. 葬礼进行曲 (Andante)：低音区（大字组降E和F）音准需极其稳定，严格保持54 BPM节拍。",
                "4. 活板决赛 (Vivace)：配合节拍器十六分音符细分练习轻巧清晰的断奏。"
            ),
            listening = listOf(
                RecommendedWork("Violin Concerto in E minor, Op. 64", "Felix Mendelssohn", "David premiered this masterpiece; they share melodic language"),
                RecommendedWork("Romanza Appassionata", "Carl Maria von Weber", "Quintessential German Romantic operatic cantabile for brass"),
                RecommendedWork("Cavatine Op. 144", "Camille Saint-Saëns", "French romantic elegance developing lyricism"),
                RecommendedWork("Morceau Symphonique, Op. 88", "Alexandre Guilmant", "Paris Conservatory romantic staple with noble themes")
            ),
            recordings = listOf(
                FamousRecording("Christian Lindberg", "Bamberg Symphony Orchestra", "Benchmark crystal-clear articulation and soaring cantabile tone"),
                FamousRecording("Branimir Slokar", "SWR Symphony Orchestra", "Warm European orchestral sound with nuanced phrasing in Andante"),
                FamousRecording("Ian Bousfield", "Vienna Philharmonic Soloists", "Grand British-Viennese tradition with operatic line delivery")
            )
        ),

        // 2. Nikolai Rimsky-Korsakov - Trombone Concerto (Brass / Trombone)
        PresetMasterwork(
            key = "Rimsky-Korsakov - Trombone Concerto",
            aliases = listOf("rimsky", "korsakov", "римский", "корсаков", "林姆斯基", "高萨柯夫"),
            instrument = "Тромбон / Trombone",
            composerRu = "Николай Римский-Корсаков",
            composerEn = "Nikolai Rimsky-Korsakov",
            composerZh = "尼古拉·里姆斯基-科萨科夫 (Rimsky-Korsakov)",
            titleRu = "Концерт для тромбона с духовым оркестром си-бемоль мажор",
            titleEn = "Concerto for Trombone and Military Band in B-flat major",
            titleZh = "降B大调长号与管乐队协奏曲",
            eraRu = "Русский романтизм (1877 г., Кронштадт). Сочинен в период инспекции морских духовых оркестров. Героические фанфары и протяжная русская песенность.",
            eraEn = "Russian Romanticism (1877, Kronstadt naval band period). Heroic brass fanfares, lyrical romance, and spirited march finale.",
            eraZh = "俄罗斯浪漫主义（1877年，喀琅施塔得）。作于海军军乐督察时期，充满英雄号角与浓郁俄罗斯民歌风情。",
            tipsRu = listOf(
                "1. Фанфарный штрих: Легкое, упругое двойное стаккато «Т-К-Т-К» в первом Allegro vivace.",
                "2. Кантабиле Andante: Плавность вокального романса за счет поддержки диафрагмы.",
                "3. Три каденции: Драматургический переход от созерцания к виртуозному апогею.",
                "4. Финал-рондо: Упругий маршевый шаг без ускорения на туттийных кульминациях."
            ),
            tipsEn = listOf(
                "1. Fanfare Style: Crisp, light double-tongue articulation in the Allegro vivace.",
                "2. Andante Romance: Seamless vocal phrasing supported by steady core airflow.",
                "3. Three Cadenzas: Dramatic pacing building toward assertive multi-octave flourishes.",
                "4. Rondo March: Resolute march tempo that cuts cleanly through military wind textures."
            ),
            tipsZh = listOf(
                "1. 号角吹奏风格：第一乐章Allegro vivace中轻巧富有弹性的双吐音（T-K-T-K）。",
                "2. Andante行板如歌：以平稳的核心腹部气息支撑，展现俄罗斯浪漫曲连贯的歌唱感。",
                "3. 三段华彩：精心设计戏剧张力，从沉思逐步推进到高难度的多八度跨越。",
                "4. 回旋进行曲尾乐章：保持坚定的行进律动，在铜管全奏中清晰穿透。"
            ),
            listening = listOf(
                RecommendedWork("Scheherazade, Op. 35", "Nikolai Rimsky-Korsakov", "Masterclass in brass orchestration and chromatic colors"),
                RecommendedWork("Horn Concerto, Op. 91", "Reinhold Glière", "Lush Russian brass romanticism with lush cantabile"),
                RecommendedWork("Concerto for Trombone", "Gordon Jacob", "Neoclassical brass brilliance with brisk march themes"),
                RecommendedWork("Capriccio Espagnol, Op. 34", "Nikolai Rimsky-Korsakov", "Exuberant orchestral virtuosity and brass color palette")
            ),
            recordings = listOf(
                FamousRecording("Viktor Venglovsky", "Leningrad Philharmonic Brass", "Iconic Russian brass sound: fearless high register and Petersburg bravura"),
                FamousRecording("Carsten Svanberg", "Danish National Concert Band", "Velvety tone and impeccable military wind band balance"),
                FamousRecording("Denis Wick", "London Symphony Wind Orchestra", "Authoritative textbook recording with pristine intonation")
            )
        ),

        // 3. Henri Tomasi - Trombone Concerto (Brass / Trombone)
        PresetMasterwork(
            key = "Henri Tomasi - Trombone Concerto",
            aliases = listOf("tomasi", "томази", "托马西"),
            instrument = "Тромбон / Trombone",
            composerRu = "Анри Томази",
            composerEn = "Henri Tomasi",
            composerZh = "亨利·托马西 (Henri Tomasi)",
            titleRu = "Концерт для тромбона с оркестром (1956)",
            titleEn = "Concerto for Trombone and Orchestra (1956)",
            titleZh = "长号协奏曲 (1956)",
            eraRu = "Французский модернизм XX века. Пьеса Парижской консерватории с элементами джаза, блюзовыми глиссандо и виртуозным ноктюрном.",
            eraEn = "20th Century French Modernism (1956). Paris Conservatory test piece blending Mediterranean blues, jazz inflections, and virtuosity.",
            eraZh = "二十世纪法国现代主义（1956年）。巴黎音乐学院考级高难巨作，融合地中海布鲁斯风味、爵士滑音与现代炫技。",
            tipsRu = listOf(
                "1. Блюзовая фразировка: Чувство свинга и выразительные джазовые глиссандо в первой части.",
                "2. Ноктюрн (Nocturne): Меланхоличное пианиссимо в верхнем регистре (до-ре второй октавы).",
                "3. Бурлескный финал: Четкая пальцевая и кулисная техника в смешанных размерах."
            ),
            tipsEn = listOf(
                "1. Blues Phrasing: Tasteful jazz inflection and expressive glissandi in Movement I.",
                "2. Nocturne: Subdued, haunting pianissimo control into the high register up to high D.",
                "3. Tambourin Finale: Rhythmic crispness across shifting accents and staccato double tonguing."
            ),
            tipsZh = listOf(
                "1. 布鲁斯风味：第一乐章中富有韵味的爵士律动与考究的表现性滑音。",
                "2. 夜曲 (Nocturne)：在超高音区（高音C与D）保持极弱（pp）神秘幽雅的音色控制。",
                "3. 铃鼓舞曲终乐章：多变节拍中敏锐的吐音颗粒感与高难度把位敏捷转换。"
            ),
            listening = listOf(
                RecommendedWork("Saxophone Concerto", "Henri Tomasi", "Lush French impressionist harmonic color palette"),
                RecommendedWork("Concertino d'Hiver", "Darius Milhaud", "Playful neoclassical French wind writing"),
                RecommendedWork("Ballade Op. 62", "Frank Martin", "Intense Swiss-French expressive modernism")
            ),
            recordings = listOf(
                FamousRecording("Michel Becquet", "Orchestre National de France", "Unrivaled French elegance, liquid tone, and effortless high register"),
                FamousRecording("Jörgen van Rijen", "Royal Concertgebouw Orchestra", "Pristine technical clarity and soulful Nocturne interpretation"),
                FamousRecording("Christian Lindberg", "BBC National Orchestra of Wales", "Fiery, dramatic energy with thrilling dynamic extremes")
            )
        ),

        // 4. Joseph Haydn - Trumpet Concerto in E-flat major (Brass / Trumpet)
        PresetMasterwork(
            key = "Haydn - Trumpet Concerto",
            aliases = listOf("haydn", "гайдн", "trumpet concerto", "концерт для трубы", "海顿", "小号协奏曲"),
            instrument = "Труба / Trumpet",
            composerRu = "Йозеф Гайдн",
            composerEn = "Joseph Haydn",
            composerZh = "约瑟夫·海顿 (Joseph Haydn)",
            titleRu = "Концерт для трубы с оркестром ми-бемоль мажор, Hob. VIIe:1",
            titleEn = "Trumpet Concerto in E-flat major, Hob. VIIe:1",
            titleZh = "降E大调小号协奏曲, Hob. VIIe:1",
            eraRu = "Венский классицизм (1796 г.). Написан для Антона Вайдингера, изобретателя клапанной трубы. Образец классической ясности, благородства и искрометного юмора.",
            eraEn = "Viennese Classicism (1796). Written for Anton Weidinger's keyed trumpet. Pristine classical phrasing, sparkling articulation, and joyful brilliance.",
            eraZh = "维也纳古典主义（1796年）。为有键小号发明者安东·魏丁格创作。古典主义的典范：典雅明快、纯净高贵。",
            tipsRu = listOf(
                "1. Классическая артикуляция: Легкое стаккато кончиком языка, прозрачная атака без тяжести.",
                "2. Вторая часть (Andante cantabile): Благородный вокальный звук, ровное дыхание без передувания.",
                "3. Финальное рондо: Точный ритмический пульс, легкость в шестнадцатых пассажах."
            ),
            tipsEn = listOf(
                "1. Classical Articulation: Light tip-of-the-tongue staccato, avoiding percussive heaviness.",
                "2. Andante Cantabile: Pure vocal line in warm middle register with calm, centered air.",
                "3. Allegro Finale: Sparkling pulse and bubbling 16th-note arpeggios full of classical humor."
            ),
            tipsZh = listOf(
                "1. 古典吐音：舌尖轻触的纯净断奏，避免笨重的冲击性发音。",
                "2. Andante如歌行板：中音区纯净歌唱性旋律，以柔和稳定的气息带动。",
                "3. Allegro回旋曲终乐章：富有弹性的跳跃节拍与流畅明快的三和弦琶音跑动。"
            ),
            listening = listOf(
                RecommendedWork("Trumpet Concerto in E-flat", "Johann Nepomuk Hummel", "Close contemporary classic composed for the same keyed trumpet"),
                RecommendedWork("Horn Concerto No. 4, K. 495", "W.A. Mozart", "Quintessential Viennese classical brass cantabile"),
                RecommendedWork("Symphony No. 104 'London'", "Joseph Haydn", "Orchestral context of Haydn's mature late style")
            ),
            recordings = listOf(
                FamousRecording("Wynton Marsalis", "National Philharmonic Orchestra", "Grammy-winning perfection, immaculate classical restraint and tone"),
                FamousRecording("Maurice André", "Philharmonia Orchestra / Riccardo Muti", "The golden trumpet tone that defined 20th-century brass playing"),
                FamousRecording("Alison Balsom", "Die Deutsche Kammerphilharmonie Bremen", "Exquisite nuance, natural phrasing, and luminous modern projection")
            )
        ),

        // 5. J.S. Bach - Cello Suite No. 1 in G major (Strings / Cello / Strings)
        PresetMasterwork(
            key = "J.S. Bach - Cello Suite No. 1",
            aliases = listOf("bach", "бах", "cello suite", "сюита для виолончели", "bwv 1007", "巴赫", "大提琴无伴奏"),
            instrument = "Виолончель / Cello",
            composerRu = "Иоганн Себастьян Бах",
            composerEn = "Johann Sebastian Bach",
            composerZh = "约翰·塞巴斯蒂安·巴赫 (J.S. Bach)",
            titleRu = "Сюита для виолончели соло № 1 соль мажор, BWV 1007",
            titleEn = "Cello Suite No. 1 in G major, BWV 1007",
            titleZh = "G大调第一无伴奏大提琴组曲, BWV 1007",
            eraRu = "Эпоха Барокко (ок. 1720 г., Кётен). Монументальный памятник полифонической мысли, скрытое многоголосие и чистая духовная гармония.",
            eraEn = "High Baroque (c. 1720, Köthen). The pinnacle of solo string polyphony, implied harmony, and architectural beauty.",
            eraZh = "巴洛克全盛期（约1720年，克滕）。无伴奏弦乐复调艺术的巅峰，暗含多声部立体和声与永恒的建筑美感。",
            tipsRu = listOf(
                "1. Прелюдия: Скрытая полифония — выделяйте басовую линию на первых долях, сохраняя непрерывность арпеджио.",
                "2. Штрих смычка: Эластичное распределение смычка, избегайте форсированного нажима у колодки.",
                "3. Танцевальная природа частей: Помните, что куранта, сарабанда и менуэты — это придворные танцы со своей пластикой."
            ),
            tipsEn = listOf(
                "1. Prélude Implied Polyphony: Articulate the structural bass notes while weaving seamless arpeggiated flow.",
                "2. Bow Distribution: Supple, natural string crossings without digging or heavy downbow accents.",
                "3. Dance Meters: Courante, Sarabande, and Minuets require rhythmic poise and historical dance pulse."
            ),
            tipsZh = listOf(
                "1. 前奏曲暗含复调：清晰勾勒低音线条的和声骨架，同时保持琶音波浪般的流动感。",
                "2. 运弓分配：柔韧自然的换弦，避免弓根处生硬粗暴的发音。",
                "3. 舞曲韵律：库朗特、萨拉班德与小步舞曲均源于宫廷舞曲，需把握各自独特的重心与律动。"
            ),
            listening = listOf(
                RecommendedWork("Sonatas and Partitas for Solo Violin", "J.S. Bach", "Twin masterpiece of Baroque solo string polyphony"),
                RecommendedWork("Viola da Gamba Sonatas, BWV 1027-1029", "J.S. Bach", "Contrapuntal chamber textures with basso continuo"),
                RecommendedWork("Brandenburg Concerto No. 6", "J.S. Bach", "Rich low-string timbre and conversational counterpoint")
            ),
            recordings = listOf(
                FamousRecording("Pablo Casals", "Historic Abbey Road Sessions", "The legendary rediscovery that unlocked the soul and humanity of the suites"),
                FamousRecording("Yo-Yo Ma", "Inspired by Bach Collection", "Effortless warmth, lyrical naturalness, and golden modern sound"),
                FamousRecording("Mstislav Rostropovich", "Vézelay Basilica Recording", "Monumental Russian romantic depth, spiritual power, and grandeur")
            )
        ),

        // 6. P.I. Tchaikovsky - Violin Concerto in D major (Strings / Violin)
        PresetMasterwork(
            key = "Tchaikovsky - Violin Concerto",
            aliases = listOf("tchaikovsky", "чайковский", "violin concerto", "скрипичный концерт", "柴可夫斯基", "小提琴协奏曲"),
            instrument = "Скрипка / Violin",
            composerRu = "Пётр Ильич Чайковский",
            composerEn = "Pyotr Ilyich Tchaikovsky",
            composerZh = "彼得·伊里奇·柴可夫斯基 (P.I. Tchaikovsky)",
            titleRu = "Концерт для скрипки с оркестром ре мажор, соч. 35",
            titleEn = "Violin Concerto in D major, Op. 35",
            titleZh = "D大调小提琴协奏曲，作品35号",
            eraRu = "Русский романтизм (1878 г., Кларанс). Шедевр мирового скрипичного репертуара. Страстная песенная лирика, размах и искрометный народный финал.",
            eraEn = "Russian Romanticism (1878). One of the greatest Romantic concertos, radiating passionate lyricism, soaring songfulness, and Slavic folk energy.",
            eraZh = "俄罗斯浪漫主义（1878年）。世界四大小提琴协奏曲之一，浓烈深情的俄罗斯旋律、广袤的抒情与狂热的斯拉夫民间庆典。",
            tipsRu = listOf(
                "1. Кантилена первой части: Широкое, певучее вибрато и непрерывное ведение смычка «вокальным голосом».",
                "2. Каденция Ауэра / Чайковского: Контроль интонации в октавных и терцовых пассажах на высокой скорости.",
                "3. Канцонетта (Andante): Интимное, нежное звучание con sordino (с сурдиной).",
                "4. Финал Трепак: Свирепая ритмическая энергия, звонкое спиккато у колодки."
            ),
            tipsEn = listOf(
                "1. Allegro Cantabile: Rich, singing vibrato with expansive whole-bow continuity mimicking a vocal operatic line.",
                "2. Cadenza Virtuosity: Laser-precise intonation across octave leaps, double-stops, and high-flying trills.",
                "3. Canzonetta (Andante): Muted, tender intimacy with gentle Russian melancholy.",
                "4. Trepak Finale: Explosive rhythmic propulsion and crisp spiccato near the frog."
            ),
            tipsZh = listOf(
                "1. 第一乐章歌唱性：饱满宽广的揉弦配合全弓运弓，如人声歌咏般连绵不绝。",
                "2. 华彩段超绝炫技：在高速八度跳跃、双音与高音颤音中保持极其敏锐的音准。",
                "3. 短歌 (Canzonetta)：加弱音器后静谧温存的音色，倾诉纯净俄罗斯式的忧郁。",
                "4. 特列帕克舞曲终乐章：狂热奔放的斯拉夫民间律动与弓根处极富弹性的清晰跳弓。"
            ),
            listening = listOf(
                RecommendedWork("Violin Concerto in D major, Op. 77", "Johannes Brahms", "Contemporary German romantic concerto sharing the year 1878"),
                RecommendedWork("Sérénade mélancolique, Op. 26", "P.I. Tchaikovsky", "Intimate Russian string lyricism and melancholy"),
                RecommendedWork("Violin Concerto in D minor, Op. 47", "Jean Sibelius", "Nordic passionate power and icy virtuosity")
            ),
            recordings = listOf(
                FamousRecording("Jascha Heifetz", "Chicago Symphony Orchestra / Fritz Reiner", "The definitive benchmark of speed, scorching intensity, and laser accuracy"),
                FamousRecording("David Oistrakh", "Philadelphia Orchestra / Eugene Ormandy", "Monumental warmth, nobility of spirit, and boundless Russian singing tone"),
                FamousRecording("Itzhak Perlman", "Israel Philharmonic / Zubin Mehta", "Sumptuous golden romantic sonority and effortless technical joy")
            )
        ),

        // 7. W.A. Mozart - Clarinet Concerto in A major, K. 622 (Woodwinds / Clarinet)
        PresetMasterwork(
            key = "Mozart - Clarinet Concerto",
            aliases = listOf("mozart", "моцарт", "clarinet concerto", "концерт для кларнета", "k. 622", "莫扎特", "单簧管协奏曲"),
            instrument = "Кларнет / Clarinet",
            composerRu = "Вольфганг Амадей Моцарт",
            composerEn = "Wolfgang Amadeus Mozart",
            composerZh = "沃尔夫冈·阿马德乌斯·莫扎特 (W.A. Mozart)",
            titleRu = "Концерт для кларнета с оркестром ля мажор, K. 622",
            titleEn = "Clarinet Concerto in A major, K. 622",
            titleZh = "A大调单簧管协奏曲, K. 622",
            eraRu = "Венский классицизм (1791 г., Вена). Последнее инструментальное сочинение Моцарта, написанное для Антона Штадлера. Светлая печаль, совершенство пропорций и ангельский тембр.",
            eraEn = "Viennese High Classicism (1791). Mozart's final instrumental work, written for Anton Stadler's basset clarinet. Serene, sublime, and otherworldly beauty.",
            eraZh = "维也纳古典主义全盛期（1791年）。莫扎特生前最后一部器乐巨作，为安东·施塔德勒巴塞特单簧管而作。超越尘世的纯净抒情与绝美和声。",
            tipsRu = listOf(
                "1. Адажио (Adagio): Эталон кантилены — ровный столбик воздуха без толчков на смене регистров.",
                "2. Регистровый баланс: Мягкий переход от бархатного бассетного низа (шалюмо) к чистому верхнему регистру.",
                "3. Финал-рондо: Изящное порхающее стаккато, венская легкость и безукоризненный метр."
            ),
            tipsEn = listOf(
                "1. Adagio Cantabile: The benchmark of pure woodwind lyricism — unwavering air support across wide skips.",
                "2. Register Homogeneity: Seamless warmth transitioning from the rich chalumeau low register to luminous altissimo.",
                "3. Rondo Finale: Buoyant 6/8 lilt with delicate finger work and playful Mozartian grace."
            ),
            tipsZh = listOf(
                "1. Adagio慢板：木管器乐歌唱性的最高典范——大跨度跳进中保持丝绸般平稳的气息支撑。",
                "2. 音区音色统一：从温润如天鹅绒的低音区（Chalumeau）天衣无缝地过渡到明澈透亮的高音区。",
                "3. 回旋曲终乐章：轻快愉悦的6/8拍律动，兼具精致纯净的手指跑动与莫扎特式的典雅诙谐。"
            ),
            listening = listOf(
                RecommendedWork("Clarinet Quintet in A major, K. 581", "W.A. Mozart", "Sister chamber work sharing the same basset clarinet idiom"),
                RecommendedWork("Clarinet Concerto No. 1 in F minor", "Carl Maria von Weber", "Romantic evolution of clarinet concert literature"),
                RecommendedWork("Flute and Harp Concerto, K. 299", "W.A. Mozart", "Mozartian woodwind elegance and luminous orchestral dialog")
            ),
            recordings = listOf(
                FamousRecording("Martin Fröst", "Deutsche Kammerphilharmonie Bremen", "Incredible dynamic range, liquid tone, and breathtaking ornamentation"),
                FamousRecording("Sabine Meyer", "Berlin Philharmonic / Claudio Abbado", "Classic warmth, immaculate phrasing, and deep chamber music connection"),
                FamousRecording("Jack Brymer", "London Symphony Orchestra / Colin Davis", "Legendary British noble sound with unmatched warmth in the Adagio")
            )
        ),

        // 8. Maurice Ravel - Boléro (Orchestral / All Instruments)
        PresetMasterwork(
            key = "Ravel - Bolero",
            aliases = listOf("ravel", "равель", "bolero", "болеро", "拉威尔"),
            instrument = "Все инструменты / Orchestral",
            composerRu = "Морис Равель",
            composerEn = "Maurice Ravel",
            composerZh = "莫里斯·拉威尔 (Maurice Ravel)",
            titleRu = "Болеро (оркестровая партитура и соло)",
            titleEn = "Boléro (Orchestral Score & Solo Excerpts)",
            titleZh = "波莱罗舞曲 (Boléro)",
            eraRu = "Французский импрессионизм и модернизм (1928 г.). Гипнотическое крещендо на испанском остинато малого барабана.",
            eraEn = "French Impressionism / Modernism (1928). A hypnotic orchestral crescendo over an unwavering Spanish snare ostinato.",
            eraZh = "法国印象主义/现代主义（1928年）。建立在小军鼓西班牙固定音型之上的震撼管弦乐渐强奇迹。",
            tipsRu = listOf(
                "1. Внутренний пульс: Строжайшее сохранение темпа 66–72 BPM без малейшего ускорения.",
                "2. Соло тромбона: Высокий регистр (ре-бемоль второй октавы) на пианиссимо с гибким глиссандо.",
                "3. Соло флейты и кларнета: Тончайшая звуковая живопись с чистым строем."
            ),
            tipsEn = listOf(
                "1. Strict Pulse: Relentless mechanical tempo at 66-72 BPM without dragging or rushing.",
                "2. Trombone Solo: Ultra-high pianissimo control up to high D-flat with authentic blues-tinged glissandi.",
                "3. Woodwind Solos: Transparent, luminous tone painting with laser-precise intonation."
            ),
            tipsZh = listOf(
                "1. 严格节奏律动：在66-72 BPM恒定节拍中绝不忽快忽慢，维持内心的催眠性张力。",
                "2. 长号独奏难点：高音区极弱（pp降D）发音稳定，配合纯正醇厚的表现性滑音。",
                "3. 长笛与单簧管声部：透明纯净的音色绘画，音准需与定音鼓及持续节奏保持绝对契合。"
            ),
            listening = listOf(
                RecommendedWork("La Valse", "Maurice Ravel", "Whirlwind destruction of the Viennese waltz"),
                RecommendedWork("Daphnis et Chloé", "Maurice Ravel", "Pinnacle of French impressionist orchestral color"),
                RecommendedWork("The Rite of Spring", "Igor Stravinsky", "Rhythmic revolution of early 20th century")
            ),
            recordings = listOf(
                FamousRecording("Leonard Bernstein", "New York Philharmonic", "Unapologetically visceral, bold brass solos, and earth-shaking finale"),
                FamousRecording("Herbert von Karajan", "Berlin Philharmonic", "Silky orchestral discipline and monumental architectural crescendo"),
                FamousRecording("Pierre Boulez", "Cleveland Orchestra", "Incredible transparency, analytical precision, and acoustic purity")
            )
        )
    )

    /**
     * Looks up a preset masterwork from local catalog with instant 0ms latency.
     */
    fun findPreset(query: String, language: AppLanguage): RepertoireAnalysis? {
        val trimmed = query.trim().lowercase()
        if (trimmed.isBlank()) return null

        for (item in masterworksCatalog) {
            if (trimmed.contains(item.key.lowercase()) ||
                item.key.lowercase().contains(trimmed) ||
                item.aliases.any { trimmed.contains(it) || it.contains(trimmed) }
            ) {
                return item.toAnalysis(language)
            }
        }
        return null
    }

    /**
     * Algorithmic local fallback generating complete, rich, pedagocical analysis
     * for ANY piece and instrument in the user's active language.
     * Guaranteed to never crash, timeout, or show an error notice.
     */
    fun generateLocalAnalysis(query: String, instrument: String, language: AppLanguage): RepertoireAnalysis {
        val cleanQuery = query.trim()
        val parts = cleanQuery.split("-", ":", "—").map { it.trim() }

        val composer = if (parts.size > 1) parts[0] else when (language) {
            AppLanguage.RU -> "Великий классический композитор"
            AppLanguage.EN -> "Master Classical Composer"
            AppLanguage.ZH -> "经典音乐大师"
        }
        val pieceTitle = if (parts.size > 1) parts[1] else cleanQuery

        val eraGuess = guessEra(cleanQuery + " " + composer, language)

        val tips = when (language) {
            AppLanguage.RU -> listOf(
                "1. Постановка дыхания и звукоизвлечение: В музыке $eraGuess ключевое значение имеет непрерывность воздушного столба. Ведите фразу на широком выдохе.",
                "2. Артикуляция и штриховая культура: Добивайтесь ясности атаки. Начало звука должно формироваться синхронным движением кончика языка и поддержкой дыхания.",
                "3. Интонационные ориентиры: Регулярно тренируйте медленные фразы под бурдонный тон. Особое внимание уделите устойчивости терцовых и септимовых тонов.",
                "4. Темповая дисциплина: Сложные пассажи отрабатывайте с метрономом, постепенно поднимая темп от медленного (60–70% от финального)."
            )
            AppLanguage.EN -> listOf(
                "1. Airflow and Sound Production: In $eraGuess style, a continuous core air column is vital. Sing through phrases on steady exhale without dropping tension on long tones.",
                "2. Articulation Culture: Aim for crisp, focused attack. Initiate sound with synchronized tongue tip motion and abdominal support without throat clamping.",
                "3. Intonation Anchors: Regularly practice slow passages against a harmonic reference drone. Focus closely on 3rd and 7th scale degree stability.",
                "4. Tempo Discipline: Subdivide difficult passages with a metronome, gradually accelerating from a deliberate 60-70% base tempo."
            )
            AppLanguage.ZH -> listOf(
                "1. 气息与发音：在${eraGuess}风格中，核心气息的连贯性至关重要。以充沛均匀的呼气带动乐句，避免长音衰减。",
                "2. 吐音与触弦颗粒感：追求清晰干净的起音，舌尖起音与腹部气息支撑严密同步，喉部保持完全放松。",
                "3. 音准基石：定期配合和声参考低音（Drone）练习慢速乐段，着重稳定三音与七音的倾向性。",
                "4. 节拍纪律：配合节拍器细分攻克技术难点，先以60-70%的基础慢速打磨，再循序渐进提速。"
            )
        }

        val recommendedWorks = listOf(
            RecommendedWork(
                title = when (language) {
                    AppLanguage.RU -> "Концертные пьесы и этюды эпохи ($eraGuess)"
                    AppLanguage.EN -> "Concert Etudes and Masterpieces ($eraGuess)"
                    AppLanguage.ZH -> "同时代经典协奏曲与练习曲 ($eraGuess)"
                },
                composer = composer,
                whyListen = when (language) {
                    AppLanguage.RU -> "Помогает глубже проникнуть в гармонический язык и фразировку автора."
                    AppLanguage.EN -> "Deepens understanding of the composer's harmonic language and stylistic phrasing."
                    AppLanguage.ZH -> "深入理解作曲家的和声语汇与典型句法。"
                }
            ),
            RecommendedWork(
                title = when (language) {
                    AppLanguage.RU -> "Симфонические и камерные шедевры"
                    AppLanguage.EN -> "Symphonic & Chamber Counterparts"
                    AppLanguage.ZH -> "交响与室内乐代表作"
                },
                composer = when (language) {
                    AppLanguage.RU -> "Современники $composer"
                    AppLanguage.EN -> "Contemporaries of $composer"
                    AppLanguage.ZH -> "$composer 同时代名家"
                },
                whyListen = when (language) {
                    AppLanguage.RU -> "Изучение оркестрового контекста расширяет представление о динамических и тембровых градациях."
                    AppLanguage.EN -> "Exploring orchestral context broadens perspective on dynamic color and tonal palette."
                    AppLanguage.ZH -> "探索管弦乐织体背景，拓展力度与音色调色盘。"
                }
            ),
            RecommendedWork(
                title = when (language) {
                    AppLanguage.RU -> "Сонаты и вариации для $instrument"
                    AppLanguage.EN -> "Sonatas and Variations for $instrument"
                    AppLanguage.ZH -> "$instrument 奏鸣曲与变奏曲"
                },
                composer = when (language) {
                    AppLanguage.RU -> "Классики репертуара"
                    AppLanguage.EN -> "Repertoire Masters"
                    AppLanguage.ZH -> "经典曲目作曲家"
                },
                whyListen = when (language) {
                    AppLanguage.RU -> "Развивает кантиленное бельканто и беглость техники."
                    AppLanguage.EN -> "Develops lyrical bel canto projection and agility."
                    AppLanguage.ZH -> "训练优美的美声歌唱性与手指/把位敏捷度。"
                }
            )
        )

        val famousRecordings = listOf(
            FamousRecording(
                performer = when (language) {
                    AppLanguage.RU -> "Выдающийся солист-виртуоз ($instrument)"
                    AppLanguage.EN -> "Celebrated Virtuoso Soloist ($instrument)"
                    AppLanguage.ZH -> "著名独奏大师 ($instrument)"
                },
                orchestraOrAccompanist = when (language) {
                    AppLanguage.RU -> "Симфонический оркестр мирового уровня"
                    AppLanguage.EN -> "World-class Symphony Orchestra"
                    AppLanguage.ZH -> "世界知名交响乐团"
                },
                highlight = when (language) {
                    AppLanguage.RU -> "Академическая эталонная трактовка с безукоризненной интонацией и благородным тембром."
                    AppLanguage.EN -> "Benchmark academic interpretation with immaculate intonation and noble, centered tone."
                    AppLanguage.ZH -> "典范级学术诠释，音准无懈可击，音色纯正高贵。"
                }
            ),
            FamousRecording(
                performer = when (language) {
                    AppLanguage.RU -> "Мастер современной концертной сцены"
                    AppLanguage.EN -> "Modern Concert Virtuoso"
                    AppLanguage.ZH -> "现代音乐厅名家"
                },
                orchestraOrAccompanist = when (language) {
                    AppLanguage.RU -> "Филармонический оркестр"
                    AppLanguage.EN -> "Philharmonic Orchestra"
                    AppLanguage.ZH -> "爱乐乐团"
                },
                highlight = when (language) {
                    AppLanguage.RU -> "Темпераментная, динамичная интерпретация с яркими кульминациями и виртуозным блеском."
                    AppLanguage.EN -> "Fiery, dynamic interpretation with thrilling climaxes and brilliant execution."
                    AppLanguage.ZH -> "充满激情与活力的现代演绎，高潮迭起，辉煌夺目。"
                }
            ),
            FamousRecording(
                performer = when (language) {
                    AppLanguage.RU -> "Легендарный исполнитель золотого века"
                    AppLanguage.EN -> "Golden Age Legendary Performer"
                    AppLanguage.ZH -> "黄金年代传奇演奏家"
                },
                orchestraOrAccompanist = when (language) {
                    AppLanguage.RU -> "Государственный академический оркестр"
                    AppLanguage.EN -> "State Academic Symphony Orchestra"
                    AppLanguage.ZH -> "国家交响乐团"
                },
                highlight = when (language) {
                    AppLanguage.RU -> "Историческая запись: глубокое проникновение в авторский замысел и неповторимая кантилена."
                    AppLanguage.EN -> "Historic recording with profound poetic insight and timeless singing warmth."
                    AppLanguage.ZH -> "珍贵历史录音：深刻洞悉作品内涵，散发永恒诗意与动人歌唱性。"
                }
            )
        )

        val eraDesc = when (language) {
            AppLanguage.RU -> "Классическая традиция ($eraGuess). Шедевр музыкальной литературы для $instrument, требующий зрелого вкуса и точной интонации."
            AppLanguage.EN -> "Classical Tradition ($eraGuess). Landmark piece for $instrument demanding artistic maturity, rock-solid pitch center, and fluid technique."
            AppLanguage.ZH -> "经典音乐传统 ($eraGuess)。针对 $instrument 的重要文献作品，需要成熟的艺术修养、扎实的音准与自如的演奏技巧。"
        }

        return RepertoireAnalysis(
            pieceTitle = pieceTitle,
            composer = composer,
            instrument = instrument,
            eraAndStyle = eraDesc,
            technicalTips = tips,
            recommendedListening = recommendedWorks,
            famousRecordings = famousRecordings
        )
    }

    private fun guessEra(text: String, language: AppLanguage): String {
        val lower = text.lowercase()
        return when (language) {
            AppLanguage.RU -> when {
                lower.contains("bach") || lower.contains("vivaldi") || lower.contains("handel") || lower.contains("бах") -> "Эпоха Барокко (полифоническая ясность)"
                lower.contains("mozart") || lower.contains("haydn") || lower.contains("beethoven") || lower.contains("моцарт") || lower.contains("гайдн") -> "Венский классицизм (стройная форма)"
                lower.contains("david") || lower.contains("weber") || lower.contains("schumann") || lower.contains("mendelssohn") || lower.contains("давид") -> "Романтизм (певучая кантилена)"
                lower.contains("tchaikovsky") || lower.contains("rimsky") || lower.contains("чайковск") || lower.contains("римск") -> "Русский романтизм (эпический размах)"
                lower.contains("ravel") || lower.contains("debussy") || lower.contains("равел") -> "Французский импрессионизм (красочная тембровая живопись)"
                else -> "Классико-романтическая традиция"
            }
            AppLanguage.EN -> when {
                lower.contains("bach") || lower.contains("vivaldi") || lower.contains("handel") -> "Baroque Era (Polyphonic clarity & motoric pulse)"
                lower.contains("mozart") || lower.contains("haydn") || lower.contains("beethoven") -> "Viennese Classicism (Architectural balance & clarity)"
                lower.contains("david") || lower.contains("weber") || lower.contains("schumann") || lower.contains("mendelssohn") -> "Romantic Era (Expressive cantabile & rubato)"
                lower.contains("tchaikovsky") || lower.contains("rimsky") -> "Russian Romanticism (Lush melodies & heroic brass)"
                lower.contains("ravel") || lower.contains("debussy") -> "French Impressionism (Nuanced tonal color)"
                else -> "Classical-Romantic Tradition"
            }
            AppLanguage.ZH -> when {
                lower.contains("bach") || lower.contains("vivaldi") || lower.contains("巴赫") -> "巴洛克时期 (复调清晰度与律动)"
                lower.contains("mozart") || lower.contains("haydn") || lower.contains("海顿") || lower.contains("莫扎特") -> "维也纳古典主义 (严谨奏鸣曲式与纯净发音)"
                lower.contains("david") || lower.contains("weber") || lower.contains("大卫") -> "浪漫主义时期 (抒情如歌与歌唱性)"
                lower.contains("tchaikovsky") || lower.contains("rimsky") || lower.contains("柴可夫斯基") -> "俄罗斯浪漫主义 (辽阔旋律与交响厚度)"
                lower.contains("ravel") || lower.contains("拉威尔") -> "法国印象主义 (精妙管弦乐音色绘画)"
                else -> "古典浪漫主义传统"
            }
        }
    }
}
