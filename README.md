# GeodataConverter

ДА! УТИЛИТА КОНВЕРТИРУЕТ L2D В ФОРМАТ L2J!
!ТРЕБУЕТСЯ JDK 17!
Для компиляции потребуется Apache Ant или можно скачать из релизов актуальную версию библиотеки.
Чтоб пользоваться данной утилитой - требуется скачать проект, а так же библиотеку.
Файл библиотеки хранится в *путь к проекту*/GeodataConvertor/libs/*актуальная версия*.jar

Для конвертации из формата А в формат Б требуется установить в *путь к проекту*/GeodataConvertor/work/config/main.ini параметры:

READ_FORMAT = l2d - исходный формат конвертации
SAVE_FORMAT = l2j - выходной формат после конвертации

После чего произвести запуск "start.bat" и главной папки проекта.

Обязательно проверьте, что исходные файлы хранятся в папке *путь к проекту*/GeodataConvertor/work/*READ_FORMAT*. В ином случае - ничего не произойдет.

Теоретически можно использовать на серверах, которые не используют Acis геодвиг, данные сводились к ПТС формату.

Буду рад любой помощи с експортом в формат l2d / l2g и импортом в формат l2s (в их случае - требуется дополнительная информация о привязаном сервере).

# GeodataConverter

YES! UTILITY CONVERTS L2D TO L2J FORMAT!
!REQUIRED JDK-17!
For compilation you will need Apache Ant or you can download the current version of the library from releases.
To use this utility - you need to download the project, as well as the library.
Library file is stored in *path to project*/GeodataConvertor/libs/*actual version*.jar.

To convert from format A to format B it is required to set parameters in *project path*/GeodataConvertor/work/config/main.ini:

READ_FORMAT = l2d - source format of conversion
SAVE_FORMAT = l2j - output format after conversion

Then run "start.bat" and the main project folder.

Be sure to check that the source files are stored in the *project path*/GeodataConvertor/work/*READ_FORMAT* folder. Otherwise, nothing will happen.

Theoretically it is possible to use on servers that do not use Acis geodig, the data was reduced to PTS format.

I will be glad to any help with export to l2d / l2g format and import to l2s format (in their case - requires additional information about the bound server).
