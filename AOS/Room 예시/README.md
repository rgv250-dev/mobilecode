Room 예시 

[안드로이드 ROOM 설명](https://developer.android.com/topic/libraries/architecture/room?hl=ko).

검색 내용 키워드를 저장하기에는 내부 저장소를 쓰기에는 매번 JSON 파일로 받아서 저장 하기에는 
정말 여러가지고 문제가 있다  

그렇다면 입력된 데이터만 저장하는 정도면 간단하게 되지 않을까라는 생각에

단순하게 매 입력 값을 받아 저장하는 정도라고 생각하고 만들었다.


```

@Database(entities = [KeywordsData::class], version = 1)
abstract class RecentDatabase : RoomDatabase() {

    abstract fun keywordDao(): SearchKeyWordDao

    companion object {
        private var instance: RecentDatabase? = null

        @Synchronized
        fun getInstance(context: Context): RecentDatabase? {
            if (instance == null) {
                synchronized(RecentDatabase::class){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        RecentDatabase::class.java,
                        "keyword-database"
                    ).build()
                }
            }
            return instance
        }
    }
    
}

```

```
@Entity(tableName = "recent_word") //테이블명
data class RecentKeywordsData (
    @PrimaryKey
    var keyWordNAme: String
)

```

```
@Dao
interface SearchKeyWordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) //충돌이 발생할 경우 덮어쓰기
    fun insert(recentKeywordsData: RecentKeywordsData)

    @Query("SELECT * FROM recent_word") // recentKeywordsData 모든 값을 가져와라
    fun getAll(): List<RecentKeywordsData>

    @Query("DELETE FROM recent_word") // recentKeywordsData 테이블의 모든 값 삭제
    fun deleteAll()

}
```

대충 호출 해서 쓰는 법 
```
val db = RecentDatabase.getInstance(applicationContext)!!

 CoroutineScope(Dispatchers.IO).launch{
    val keywords = db.keywordDao().getAll() 
    //리사이클러뷰에 넣거나 화면 구성 등 하시면 됩니다.
}
```
