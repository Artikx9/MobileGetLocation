# MobileGetLocation
Мобильный клиент
Условия:
0.	Предварительно генерирует и устанавливает ключ К (32 байта) и ID устройства (передать администратору сервера для записи в базу данных.

1.	Получает координаты GPS и текущее время
2.	Формирует строку формата  time:<значение>,lat:<значение>,lng:<значение>
3.	Вычисляет ХЕШ (алгоритм SHA256) значение от полученной строки и преобразовывает его в текстовый 16-ти р. вид  0345D2F3……FE5A (размером 64 символа приведенные к верхнему регистру).  
Дополняем строку: lat:<значение>,lng:<значение>,hash:<значение>
4.	Шифруем (алгоритм AES) полученную строку на ключе К.
5.	Полученное зашифрованное значение преобразуем в base64 строку encdata.
6.	Используя архитектуру REST API в методе GET отправляем сформированные данные на сервер:
7.	<Server>:<port>/api/point? ID=<значение>&encdata=<значение>


Сервер:
1.	Получает GET запрос от клиента
2.	Делает парсинг строки ID и encdata
3.	Преобразовывает encdata из Base64 в исходный вид (бинарный). По ID клиента выбирает из базы данных его ключ К
4.	Расшифровывает строку encdata. Получаем строку time:<значение>,lat:<значение>,lng:<значение>,hash:<значение>

5.	Вычисляем ХЕШ значение от строки time:<значение>,lat:<значение>,lng:<значение>, и преобразовывает его в текстовый 16-ти р. вид  0345D2F3……FE5A (размером 64 символа).  
6.	Сравниваем полученное ХЕШ-значение с принятым в поле hash
7.	Если хеш совпали, то отправляем клиенту код 200, если нет – код 400. 
8.	Записываем в базу данных полученные данные клиента и признак корректности hash (поле 0- верно /1- ошибка).

9.	При подключении клиента из браузера на <Server>:<port> вывести маршрут всех клиентов на карту Google map.
