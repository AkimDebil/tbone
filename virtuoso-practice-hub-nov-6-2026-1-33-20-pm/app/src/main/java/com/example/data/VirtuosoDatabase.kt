package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.PracticeLogDao
import com.example.data.dao.RepertoireDao
import com.example.data.dao.RoutineDao
import com.example.data.entity.PracticeLogEntity
import com.example.data.entity.PracticeRoutineEntity
import com.example.data.entity.SavedRepertoireEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Database(
    entities = [
        PracticeRoutineEntity::class,
        SavedRepertoireEntity::class,
        PracticeLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VirtuosoDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun repertoireDao(): RepertoireDao
    abstract fun practiceLogDao(): PracticeLogDao

    companion object {
        @Volatile
        private var INSTANCE: VirtuosoDatabase? = null

        fun getDatabase(context: Context): VirtuosoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VirtuosoDatabase::class.java,
                    "virtuoso_hub.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default data on first creation
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    seedDefaultData(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedDefaultData(database: VirtuosoDatabase) {
            val routineDao = database.routineDao()
            val repertoireDao = database.repertoireDao()
            val logDao = database.practiceLogDao()

            // 1. Seed 60-minute classical practice routine
            val defaultRoutines = listOf(
                PracticeRoutineEntity(
                    stageType = "WARM_UP",
                    title = "Разминка / Длинные тона",
                    subtitle = "Обертоновые ряды, гибкость амбушюра и опора дыхания",
                    durationMinutes = 15,
                    targetBpm = 72,
                    timeSignature = "4/4",
                    sessionNotes = "Фокус на плотном резонирующем звуке; 4 доли на тон, атака дыханием без зажатости языка.",
                    isCompleted = true,
                    orderIndex = 0
                ),
                PracticeRoutineEntity(
                    stageType = "TECHNICAL",
                    title = "Технические этюды",
                    subtitle = "Характерные этюды Арбана и двойное стаккато",
                    durationMinutes = 15,
                    targetBpm = 104,
                    timeSignature = "4/4",
                    sessionNotes = "Этюд №11: чёткая артикуляция «Т-К-Т-К» в среднем регистре; расслабленный язык.",
                    isCompleted = true,
                    orderIndex = 1
                ),
                PracticeRoutineEntity(
                    stageType = "REPERTOIRE",
                    title = "Работа над репертуаром",
                    subtitle = "Концертино Фердинанда Давида и Концерт Римского-Корсакова",
                    durationMinutes = 20,
                    targetBpm = 88,
                    timeSignature = "4/4",
                    sessionNotes = "Часть I Allegro maestoso: фразировка тактов 42-65; чистые переходы кулисы без глиссандо.",
                    isCompleted = false,
                    orderIndex = 2
                ),
                PracticeRoutineEntity(
                    stageType = "SIGHT_READING",
                    title = "Чтение с листа и трудности",
                    subtitle = "Болеро Равеля и Полет валькирий Вагнера",
                    durationMinutes = 10,
                    targetBpm = 66,
                    timeSignature = "3/4",
                    sessionNotes = "Болеро: контроль ре-бемоль второй октавы на Pianissimo; стабильный внутренний пульс.",
                    isCompleted = false,
                    orderIndex = 3
                )
            )
            routineDao.insertAll(defaultRoutines)

            // 2. Seed Pre-loaded Repertoire Analyses (with clickable YouTube links)
            // Piece 1: Ferdinand David - Concertino Op. 4
            val davidTips = JSONArray().apply {
                put("1. Артикуляция каденции: Вступительная каденция требует героического, декламационного тона. Не торопите пассажи тридцать вторыми.")
                put("2. Кантабиле побочной темы: Сохраняйте непрерывное дыхание при смене позиций кулисы (такты 56-78), добиваясь истинного легато без подъездов.")
                put("3. Траурный марш (Andante): Точный звуковысотный центр в нижнем регистре (ми-бемоль и фа); строгий темп 54 BPM.")
                put("4. Виртуозный финал (Vivace): Отрабатывайте чередование стаккато с метрономом на шестнадцатые доли, избегая ускорения.")
            }.toString()

            val davidListening = JSONArray().apply {
                put(JSONObject().apply {
                    put("title", "Скрипичный концерт ми минор, соч. 64")
                    put("composer", "Феликс Мендельсон")
                    put("whyListen", "Фердинанд Давид был концертмейстером оркестра Гевандхауса и первым исполнителем этого концерта; шедевры объединяет романтический мелодический строй.")
                })
                put(JSONObject().apply {
                    put("title", "Страстная романса (Romanza Appassionata)")
                    put("composer", "Карл Мария фон Вебер")
                    put("whyListen", "Эталон ранней немецкой романтической фразировки и оперного вокального кантабиле.")
                })
                put(JSONObject().apply {
                    put("title", "Каватина, соч. 144")
                    put("composer", "Камиль Сен-Санс")
                    put("whyListen", "Французское романтическое изящество, демонстрирующее гибкость кантилены и позиций.")
                })
                put(JSONObject().apply {
                    put("title", "Симфоническая пьеса, соч. 88")
                    put("composer", "Александр Гильман")
                    put("whyListen", "Классическое романтическое конкурсное произведение с благородными мелодическими линиями.")
                })
            }.toString()

            val davidRecordings = JSONArray().apply {
                put(JSONObject().apply {
                    put("performer", "Кристиан Линдберг (Christian Lindberg)")
                    put("orchestraOrAccompanist", "Бамбергский симфонический оркестр")
                    put("highlight", "Эталонная трактовка, сочетающая кристальную точность техники с парящим певучим звуком.")
                })
                put(JSONObject().apply {
                    put("performer", "Бранимир Слокар (Branimir Slokar)")
                    put("orchestraOrAccompanist", "Симфонический оркестр SWR")
                    put("highlight", "Тёплый европейский оркестровый звук с изысканными нюансами в марше Andante.")
                })
                put(JSONObject().apply {
                    put("performer", "Иэн Боусфилд (Ian Bousfield)")
                    put("orchestraOrAccompanist", "Солисты Венского филармонического оркестра")
                    put("highlight", "Величественный академический тон с оперной фразировкой во второй части.")
                })
            }.toString()

            repertoireDao.insertPiece(
                SavedRepertoireEntity(
                    title = "Концертино ми-бемоль мажор, соч. 4",
                    composer = "Фердинанд Давид",
                    instrument = "Тромбон",
                    eraAndStyle = "Ранний немецкий романтизм (сочинено в 1837 г. для виртуоза Карла Трауготта Квайссера). Благородный, театральный, экспрессивный лиризм в строгой классической форме.",
                    status = "В работе (Учу)",
                    technicalTipsJson = davidTips,
                    recommendedListeningJson = davidListening,
                    famousRecordingsJson = davidRecordings,
                    userNotes = "Работаю над открывающей темой maestoso и траурным маршем Andante."
                )
            )

            // Piece 2: Nikolai Rimsky-Korsakov - Trombone Concerto
            val rkTips = JSONArray().apply {
                put("1. Русский фанфарный стиль: Чёткая, энергичная артикуляция двойным ударом языка в первой части Allegro vivace.")
                put("2. Кантабиле Andante: Специфика русской протяжной песни; связывайте фразы без толчков дыхания и кулисы.")
                put("3. Каденции: Три виртуозные каденции требуют выверенной стилистической драматургии перед маршем Allegretto.")
                put("4. Баланс с духовым оркестром: Звук солиста должен прорезать плотную фактуру духового оркестра без передувания.")
            }.toString()

            val rkListening = JSONArray().apply {
                put(JSONObject().apply {
                    put("title", "Шехеразада, соч. 35")
                    put("composer", "Николай Римский-Корсаков")
                    put("whyListen", "Вершина медной оркестровки Римского-Корсакова с хроматическими пассажами и восточным колоритом.")
                })
                put(JSONObject().apply {
                    put("title", "Концерт для валторны с оркестром си-бемоль мажор, соч. 91")
                    put("composer", "Рейнгольд Глиэр")
                    put("whyListen", "Роскошная позднеромантическая русская кантилена для медных духовых инструментов.")
                })
                put(JSONObject().apply {
                    put("title", "Концерт для тромбона с оркестром")
                    put("composer", "Гордон Джейкоб")
                    put("whyListen", "Блестящее неоклассическое сочинение с богатыми каденциями и маршевыми темами.")
                })
                put(JSONObject().apply {
                    put("title", "Испанское каприччио, соч. 34")
                    put("composer", "Николай Римский-Корсаков")
                    put("whyListen", "Феерическая виртуозность оркестровых медных и яркая тембровая палитра.")
                })
            }.toString()

            val rkRecordings = JSONArray().apply {
                put(JSONObject().apply {
                    put("performer", "Виктор Венгловский")
                    put("orchestraOrAccompanist", "Брасс-ансамбль Ленинградской филармонии")
                    put("highlight", "Легендарный ленинградский звук: смелый верхний регистр и подлинный петербургский размах.")
                })
                put(JSONObject().apply {
                    put("performer", "Карстен Сванберг (Carsten Svanberg)")
                    put("orchestraOrAccompanist", "Датский национальный духовой оркестр")
                    put("highlight", "Благородный бархатный тембр и безупречный баланс с военным оркестром.")
                })
                put(JSONObject().apply {
                    put("performer", "Денис Уик (Denis Wick)")
                    put("orchestraOrAccompanist", "Духовой оркестр Лондонского симфонического оркестра")
                    put("highlight", "Хрестоматийная академическая запись с кристальным строем и классической сдержанностью.")
                })
            }.toString()

            repertoireDao.insertPiece(
                SavedRepertoireEntity(
                    title = "Концерт для тромбона с духовым оркестром си-бемоль мажор",
                    composer = "Николай Римский-Корсаков",
                    instrument = "Тромбон",
                    eraAndStyle = "Русский романтизм (1877 г., кронштадтский период службы инспектором морских оркестров). Героические интонации, фольклорные каденции и виртуозный марш.",
                    status = "Шлифовка",
                    technicalTipsJson = rkTips,
                    recommendedListeningJson = rkListening,
                    famousRecordingsJson = rkRecordings,
                    userNotes = "Целевой темп: Allegro vivace — 116 BPM."
                )
            )

            // 3. Seed Realistic Practice Session Logs for the past 5 days
            val dayMs = 24 * 60 * 60 * 1000L
            val now = System.currentTimeMillis()
            val logs = listOf(
                PracticeLogEntity(
                    timestamp = now - (dayMs * 4),
                    durationMinutes = 60,
                    stagesCompletedCount = 4,
                    routineTitle = "Комплексное студийное занятие",
                    sessionNotes = "Продуктивное занятие. Этюд Арбана №11 разогнан до 100 BPM. Вступление к первой части Давида выучено наизусть."
                ),
                PracticeLogEntity(
                    timestamp = now - (dayMs * 3),
                    durationMinutes = 55,
                    stagesCompletedCount = 4,
                    routineTitle = "Фокус на звукоизвлечении и строе",
                    sessionNotes = "Длинные тона в квартвентильном регистре; чистые переходы кулисы в Andante Давида."
                ),
                PracticeLogEntity(
                    timestamp = now - (dayMs * 2),
                    durationMinutes = 70,
                    stagesCompletedCount = 4,
                    routineTitle = "Погружение в репертуар",
                    sessionNotes = "Отработка 2-й и 3-й каденций Римского-Корсакова; ритмическая пульсация шестнадцатыми в Болеро."
                ),
                PracticeLogEntity(
                    timestamp = now - (dayMs * 1),
                    durationMinutes = 50,
                    stagesCompletedCount = 3,
                    routineTitle = "Выносливость и артикуляция",
                    sessionNotes = "Двойное стаккато на 108 BPM. Финальные пассажи Vivace у Давида."
                ),
                PracticeLogEntity(
                    timestamp = now - (60 * 60 * 1000L), // Earlier today
                    durationMinutes = 45,
                    stagesCompletedCount = 2,
                    routineTitle = "Дневная разминка и Давид соч. 4",
                    sessionNotes = "Разминочные длинные тона выполнены. Чистый строй на си-бемоль первой октавы."
                )
            )
            logDao.insertAllLogs(logs)
        }
    }
}
