// Ожидание полной загрузки DOM перед выполнением скрипта
document.addEventListener('DOMContentLoaded', () => {
    // Ссылки на элементы DOM для компонентов интерфейса
    const toggleReceive = document.getElementById('toggleReceive'); // Чекбокс для включения/выключения получения данных
    const toggleSend = document.getElementById('toggleSend'); // Чекбокс для включения/выключения отправки команд
    const poiTableBody = document.querySelector('#poiTable tbody'); // Тело таблицы для списка POI
    const addPoiBtn = document.getElementById('addPoi'); // Кнопка для добавления нового POI
    const robotsStatusDiv = document.getElementById('robotsStatus'); // Контейнер для карточек статуса роботов
    const indL1 = document.getElementById('indL1'); // Индикатор лампы 1
    const indL2 = document.getElementById('indL2'); // Индикатор лампы 2
    const indL3 = document.getElementById('indL3'); // Индикатор лампы 3
    const indL4 = document.getElementById('indL4'); // Индикатор лампы 4
    const sendCmdBtn = document.getElementById('sendCommand'); // Кнопка для отправки ручной команды
    const queueTableBody = document.querySelector('#queueTable tbody'); // Тело таблицы для очереди команд
    const refreshQueueBtn = document.getElementById('refreshQueue'); // Кнопка для обновления очереди команд
    const eventLogDiv = document.getElementById('eventLog'); // Контейнер для лога событий
    let wsStatus; // Экземпляр WebSocket для обновления статусов

    // Инициализация: загрузка списка POI, статусов, очереди команд и запуск WebSocket
    fetch('http://localhost:8080/api/poi')
        .then(res => res.json())
        .then(data => renderPoi(data)); // Загрузка и отображение списка POI

    console.log("123")
    fetchStatuses(); // Загрузка начальных статусов
    loadQueue(); // Загрузка очереди команд
    startStatusWS(); // Запуск WebSocket для обновления статусов

    // Обработчик переключателя получения данных
    toggleReceive.addEventListener('change', () => {
        if (toggleReceive.checked) {
            startStatusWS(); // Запуск WebSocket при включении
        } else {
            stopStatusWS(); // Остановка WebSocket при выключении
        }
        // Сохранение конфигурации на сервере
        fetch('/api/opconfig', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ receive: toggleReceive.checked })
        });
    });

    // Обработчик переключателя отправки команд
    toggleSend.addEventListener('change', () => {
        // Сохранение конфигурации на сервере
        fetch('/api/opconfig', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ send: toggleSend.checked })
        });
    });

    // Обработчик для добавления нового POI
    addPoiBtn.addEventListener('click', () => {
        // Получение значений из полей ввода для нового POI
        const name = document.getElementById('poiName').value;
        const X = document.getElementById('poiX').value;
        const Y = document.getElementById('poiY').value;
        const T = document.getElementById('poiT').value;
        // Проверка заполнения всех полей
        if (!name || !X || !Y || !T) {
            alert('Заполните все поля POI');
            return;
        }
        // Отправка нового POI на сервер
        fetch('http://localhost:8080/api/poi', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, X, Y, T })
            })
            .then(res => res.json())
            .then(() => fetch('http://localhost:8080/api/poi')) // Обновление списка POI
            .then(res => res.json())
            .then(data => renderPoi(data)); // Отображение обновленного списка POI
    });

    /**
     * Отрисовка списка POI в таблице
     * @param {Array} list - Массив объектов POI { name, X, Y, T }
     */
    function renderPoi(list) {
        poiTableBody.innerHTML = ''; // Очистка текущего содержимого таблицы
        list.forEach(p => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${p.name}</td>
                <td>${p.X}</td>
                <td>${p.Y}</td>
                <td>${p.T}</td>
                <td><button class="goPoi" data-x="${p.X}" data-y="${p.Y}" data-t="${p.T}">Перейти</button></td>
                <td><button class="delPoi" data-name="${p.name}">Удалить</button></td>
            `;
            poiTableBody.appendChild(tr); // Добавление строки в таблицу
        });

        // Добавление обработчиков для кнопок "Перейти"
        document.querySelectorAll('.goPoi').forEach(btn => btn.addEventListener('click', e => {
            const X = parseFloat(e.target.dataset.x);
            const Y = parseFloat(e.target.dataset.y);
            const T = parseFloat(e.target.dataset.t);
            const deviceId = parseInt(prompt('Введите ID устройства (например, 1 или 2):'), 10);

            if (!toggleSend.checked || isNaN(deviceId)) return; // Проверка, включена ли отправка и валиден ли deviceId

            // Отправка команды для перемещения к POI
            sendCommand({ deviceId, X, Y, T, G: 0, V: 0, lights: [] });
        }));

        // Добавление обработчиков для кнопок "Удалить"
        document.querySelectorAll('.delPoi').forEach(btn => btn.addEventListener('click', e => {
            const name = e.target.dataset.name;
            fetch(`http://localhost:8080/api/poi/${encodeURIComponent(name)}`, { method: 'DELETE' })
                .then(() => fetch('http://localhost:8080/api/poi')) // Обновление списка POI
                .then(res => res.json())
                .then(data => renderPoi(data)); // Отображение обновленного списка POI
        }));
    }

    /**
     * Запуск WebSocket для получения обновлений статусов
     */
    function startStatusWS() {
        wsStatus = new WebSocket('ws://localhost:8080/api/statuses/ws/status');
        wsStatus.onmessage = event => {
            const state = JSON.parse(event.data);
            updateStatus(state); // Обновление интерфейса новым статусом
            appendEventLog(`Статус обновлён: ${JSON.stringify(state)}`); // Логирование обновления статуса
        };
    }

    /**
     * Остановка WebSocket для обновления статусов
     */
    function stopStatusWS() {
        if (wsStatus) wsStatus.close(); // Закрытие WebSocket, если он существует
    }

    /**
     * Загрузка текущих статусов через REST API
     */
    function fetchStatuses() {
        fetch('http://localhost:8080/api/statuses')
            .then(res => res.json())
            .then(state => updateStatus(state)); // Обновление интерфейса полученными статусами
    }

    /**
     * Обновление отображения статусов роботов и ламп
     * @param {Object} state - Объект состояния { robots: [...], lamps: { L1, L2, L3, L4 } }
     */
    function updateStatus(state) {
        robotsStatusDiv.innerHTML = ''; // Очистка текущих карточек статуса роботов
        state.robots.forEach(r => {
            const card = document.createElement('div');
            card.className = 'status-card';
            card.innerHTML = `
                <strong>${r.name}</strong><br>
                X:${r.X} Y:${r.Y} T:${r.T}
            `;
            // Установка цвета индикатора: синий для s==1, зеленый для других
            const ind = document.createElement('span');
            ind.className = 'indicator ' + (r.s == 1 ? 'indicator-blue' : 'indicator-green');
            card.appendChild(ind);
            robotsStatusDiv.appendChild(card); // Добавление карточки в контейнер
        });
        // Обновление индикаторов ламп (красный для 1, зеленый для 0)
        // Количество ламп точно 4, поэтому просто перебираем
        setLamp(indL1, state.lamps.L1, 1);
        setLamp(indL2, state.lamps.L2, 2);
        setLamp(indL3, state.lamps.L3, 3);
        setLamp(indL4, state.lamps.L4, 4);

    }

    sendCmdBtn.addEventListener('click', () => {
        // Получение параметров команды из полей ввода
        const deviceId = parseInt(document.getElementById('cmdDeviceId').value, 10);
        const X = parseFloat(document.getElementById('cmdX').value);
        const Y = parseFloat(document.getElementById('cmdY').value);
        const T = parseFloat(document.getElementById('cmdT').value);
        const G = parseInt(document.getElementById('cmdG').value, 10);
        const V = parseInt(document.getElementById('cmdV').value, 10);

        // Сбор состояний выбранных ламп
        const lights = [];
        if (document.getElementById('cmdL1').checked) lights.push('L1');
        if (document.getElementById('cmdL2').checked) lights.push('L2');
        if (document.getElementById('cmdL3').checked) lights.push('L3');
        if (document.getElementById('cmdL4').checked) lights.push('L4');

        if (!toggleSend.checked) return; // Проверка, включена ли отправка

        // Отправка команды на сервер
        sendCommand({ deviceId, X, Y, T, G, V, lights });
    });

    /**
     * Обновляет цвет индикатора lampIndex:
     *  - если val == 1 → зажигает свой цвет,
     *  - иначе → переводит в серый (off).
     * @param {HTMLElement} indicator — сам <span class="indicator" id="indL#">
     * @param {number} val — 0 или 1
     * @param {number} lampIndex — 1..4 (номер лампы)
     */
    function setLamp(indicator, val, lampIndex) {
        // Сначала убираем из индикатора все цветовые классы
        indicator.className = 'indicator';

        if (val == 1) {
            // Включаем «свой» цвет в зависимости от lampIndex
            switch (lampIndex) {
                case 1:
                    indicator.classList.add('lamp-red');
                    break;
                case 2:
                    indicator.classList.add('lamp-yellow');
                    break;
                case 3:
                    indicator.classList.add('lamp-green');
                    break;
                case 4:
                    indicator.classList.add('lamp-blue');
                    break;
            }
        }
        // если val == 0, оставляем только 'indicator' (серый фон)
    }

    /**
     * Отправка команды через REST API и обновление очереди
     * @param {Object} cmd - Объект команды { deviceId, X, Y, T, G, V, lights }
     */
    function sendCommand(cmd) {
        console.log(JSON.stringify(cmd)); // Логирование команды для отладки
        fetch('http://localhost:8080/api/commands', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(cmd)
            })
            .then(res => res.json())
            .then(() => {
                appendEventLog(`Команда #${cmd.n} добавлена в очередь`); // Логирование добавления команды
                loadQueue(); // Обновление очереди команд
            });
    }

    /**
     * Загрузка и отображение очереди команд
     */
    function loadQueue() {
        fetch('http://localhost:8080/api/commands')
            .then(res => res.json())
            .then(data => {
                queueTableBody.innerHTML = ''; // Очистка текущего содержимого таблицы
                data.forEach(item => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td>${item.n}</td>
                        <td>${item.device}</td>
                        <td>${JSON.stringify(item.params)}</td>
                        <td>${item.status}</td>
                        <td>${item.timestamp}</td>
                        <td><button class="delCmd" data-id="${item.n}">Удалить</button></td>
                    `;
                    queueTableBody.appendChild(tr); // Добавление строки в таблицу
                });
                // Добавление обработчиков для кнопок "Удалить"
                document.querySelectorAll('.delCmd').forEach(btn => btn.addEventListener('click', e => {
                    const id = e.target.dataset.id;
                    fetch(`http://localhost:8080/api/commands/${id}`, { method: 'DELETE' })
                        .then(() => {
                            appendEventLog(`Команда #${id} удалена`); // Логирование удаления команды
                            loadQueue(); // Обновление очереди команд
                        });
                }));
            });
    }

    // Обработчик кнопки обновления очереди
    refreshQueueBtn.addEventListener('click', loadQueue);

    /**
     * Добавление записи в лог событий с временной меткой (в начало)
     * @param {string} text - Сообщение для лога
     */
    function appendEventLog(text) {
        const div = document.createElement('div');
        const ts = new Date().toLocaleString();
        div.textContent = `${ts} - ${text}`;
        eventLogDiv.prepend(div); // Добавление записи в начало лога
    }

    // Обработчик для очистки лога событий
    document.getElementById('clearEvents').addEventListener('click', () => {
        eventLogDiv.innerHTML = ''; // Очистка контейнера лога
    });

    // Обработчик для переключения на интерфейс инженера
    document.getElementById('switchRoleOp').addEventListener('click', () => {
        window.location.href = '../engineer/index.html'; // Перенаправление на интерфейс инженера
    });
});