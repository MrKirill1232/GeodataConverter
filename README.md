# GeodataConverter

ДА! УТИЛИТА КОНВЕРТИРУЕТ L2D В ФОРМАТ L2J!
<br>
**ПОДДЕРЖИВАЕМЫЕ ИМПОРТ ФОРМАТЫ: L2J, L2D, L2M, L2G, L2S, _path.txt (pandas style), _conv.dat, RP**
<br>
**ПОДДЕРЖИВАЕМЫЕ ЕКСПОРТ ФОРМАТЫ: L2J, L2D, L2G, _conv.dat**
<br>
!ТРЕБУЕТСЯ JDK ~~17~~ 8 (after version 1.8)!

Для компиляции потребуется Apache Ant или можно скачать из релизов актуальную версию библиотеки.
Чтоб пользоваться данной утилитой - требуется скачать проект, а так же библиотеку.
Файл библиотеки хранится в *путь к проекту*/GeodataConvertor/libs/*актуальная версия*.jar

Для конвертации из формата А в формат Б требуется установить в *путь к проекту*/GeodataConvertor/work/config/main.ini параметры:

READ_FORMAT = l2d - исходный формат конвертации
SAVE_FORMAT = l2j - выходной формат после конвертации (вы можете указать несколько форматов через `;`)

После чего произвести запуск "start.bat" и главной папки проекта.

Обязательно проверьте, что исходные файлы хранятся в папке *путь к проекту*/GeodataConvertor/work/*input*. В ином случае - ничего не произойдет.

Теоретически можно использовать на серверах, которые не используют Acis геодвиг, данные сводились к ПТС формату.

**Примечания:**
<br>
Данная программа позволяет експортировать данные из формата _path.txt в _conv.dat так же как делает это pandas.
<br>
Разработка более точного вывода геоданных из формата _path.txt в формат _conv.dat происходит в приватном режиме.  

# GeodataConverter

YES! UTILITY CONVERTS L2D TO L2J FORMAT!
<br>
**SUPPORTED IMPORT FORMATS: L2J, L2D, L2M, L2G, L2S, _path.txt (pandas style), _conv.dat, RP**
<br>
**SUPPORTED EXPORT FORMATS: L2J, L2D, L2G, _conv.dat**
<br>
!REQUIRED JDK ~~17~~ 8 (after version 1.8)!

For compilation, you will need Apache Ant or you can download the current version of the library from releases.
To use this utility - you need to download the project, as well as the library.
Library file is stored in *path to project*/GeodataConvertor/libs/*actual version*.jar.

To convert from format A to format B it is required to set parameters in *project path*/GeodataConvertor/work/config/main.ini:

READ_FORMAT = l2d - source format of conversion
SAVE_FORMAT = l2j - output format after conversion (supports multiply formats, which been split by `;`)

Then run "start.bat" and the main project folder.

Be sure to check that the source files are stored in the *project path*/GeodataConvertor/work/*input* folder. Otherwise, nothing will happen.

Theoretically it is possible to use on servers that do not use Acis geo-engine, the data was reduced to PTS format.

**Note:**
<br>
This tool supports export from format _path.txt in _conv.dat and do that as pandas convertor.
<br>
Developing more accurate version from format _path.txt in _conv.dat continue in private mode.