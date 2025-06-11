// Ожидание полной загрузки DOM перед выполнением скрипта
document.addEventListener('DOMContentLoaded', () => {
    // Ссылки на элементы интерфейса
    const toggleCollect = document.getElementById('toggleCollect'); // Чекбокс для включения/выключения сбора данных
    const toggleSave = document.getElementById('toggleSave'); // Чекбокс для включения/выключения сохранения в БД
    const updateFreq = document.getElementById('updateFreq'); // Поле для частоты обновления
    const saveThresholdsBtn = document.getElementById('saveThresholds'); // Кнопка сохранения порогов
    const rawTableBody = document.querySelector('#rawDataTable tbody'); // Тело таблицы сырых данных
    const physTableBody = document.querySelector('#physicalDataTable tbody'); // Тело таблицы физических данных
    const alertLogDiv = document.getElementById('alertLog'); // Контейнер для лога критических событий
    let page = 1; // Текущая страница истории
    let totalPages = 1; // Общее количество страниц для навигации
    let wsData, wsAlerts; // Экземпляры WebSocket для данных и алертов

    // Список параметров, для которых задаются пороговые значения
    const params = [
        'm1', 'm2', 'm3', 'm4', 'm5', 'm6',
        't1', 't2', 't3', 't4', 't5', 't6',
        'l1', 'l2', 'l3', 'l4', 'l5', 'l6',
        'X', 'Y', 'T', 'G', 'V'
    ];

    /**
     * Динамическое создание полей ввода для пороговых значений
     */
    function renderThresholdInputs() {
        const container = document.getElementById('thresholds');
        container.innerHTML = ''; // Очистка контейнера
        params.forEach(param => {
            // Поля для Warning-диапазона
            const warnLabel = document.createElement('label');
            warnLabel.textContent = `Warning ${param}`;
            const warnMin = document.createElement('input');
            warnMin.type = 'number';
            warnMin.id = `warn_${param}_min`;
            warnMin.placeholder = 'Min';
            const warnMax = document.createElement('input');
            warnMax.type = 'number';
            warnMax.id = `warn_${param}_max`;
            warnMax.placeholder = 'Max';

            // Поля для Critical-диапазона
            const critLabel = document.createElement('label');
            critLabel.textContent = `Critical ${param}`;
            const critMin = document.createElement('input');
            critMin.type = 'number';
            critMin.id = `crit_${param}_min`;
            critMin.placeholder = 'Min';
            const critMax = document.createElement('input');
            critMax.type = 'number';
            critMax.id = `crit_${param}_max`;
            critMax.placeholder = 'Max';

            // Добавление элементов в контейнер
            container.appendChild(warnLabel);
            container.appendChild(warnMin);
            container.appendChild(warnMax);
            container.appendChild(critLabel);
            container.appendChild(critMin);
            container.appendChild(critMax);
        });
    }
    renderThresholdInputs(); // Вызов функции для начальной отрисовки полей

    /**
     * Загрузка сохраненных порогов с сервера и заполнение полей
     */
    function loadThresholds() {
        fetch('http://localhost:8080/api/thresholds')
            .then(res => res.json())
            .then(data => {
                // Предполагается, что данные имеют формат { m1: { warn_min, warn_max, crit_min, crit_max }, ... }
                params.forEach(param => {
                    const warnMin = data[param].warn_min
                    const warnMax = data[param].warn_max
                    const critMin = data[param].crit_min
                    const critMax = data[param].crit_max
                    document.getElementById(`warn_${param}_min`).value = warnMin;
                    document.getElementById(`warn_${param}_max`).value = warnMax;
                    document.getElementById(`crit_${param}_min`).value = critMin;
                    document.getElementById(`crit_${param}_max`).value = critMax;
                });
            });
    }

    /**
     * Сбор значений порогов из полей и отправка на сервер
     */
    function saveThresholds() {
        const payload = {};
        params.forEach(param => {
            const warnMin = document.getElementById(`warn_${param}_min`).value;
            const warnMax = document.getElementById(`warn_${param}_max`).value;
            const critMin = document.getElementById(`crit_${param}_min`).value;
            const critMax = document.getElementById(`crit_${param}_max`).value;
            payload[param] = {
                warn_min: warnMin,
                warn_max: warnMax,
                crit_min: critMin,
                crit_max: critMax
                // warning: { min: warnMin, max: warnMax },
                // critical: { min: critMin, max: critMax }
            };
        });
        fetch('http://localhost:8080/api/thresholds', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        }).then(() => alert('Пороги сохранены'));
    }

    // Инициализация: загрузка конфигурации и порогов
    fetch('/api/config')
        .then(res => res.json())
        .then(cfg => {
            updateFreq.value = cfg.frequency; // Установка частоты обновления
            toggleSave.checked = cfg.save; // Установка флага сохранения
            toggleCollect.checked = cfg.collect; // Установка флага сбора данных
            if (toggleCollect.checked) startDataWS(); // Запуск WebSocket для данных, если сбор включен
            startAlertsWS(); // Запуск WebSocket для алертов
        });
    loadThresholds(); // Загрузка порогов

    if (toggleCollect.checked) {
        startDataWS(); // Запуск WebSocket при включении
        startAlertsWS();
    }

    // Обработчики событий для переключателей и поля частоты
    toggleCollect.addEventListener('change', () => {
        if (toggleCollect.checked) {
            startDataWS(); // Запуск WebSocket при включении
            startAlertsWS();
        } else {
            stopDataWS(); // Остановка WebSocket при выключении
            stopAlertsWS();
        }
        fetch('/api/config', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ collect: toggleCollect.checked })
        });
    });

    toggleSave.addEventListener('change', () => {
        fetch('/api/config', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ save: toggleSave.checked })
        });
    });

    updateFreq.addEventListener('change', () => {
        fetch('/api/config', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ frequency: updateFreq.value })
        });
    });

    // Обработчик для сохранения порогов
    saveThresholdsBtn.addEventListener('click', saveThresholds);

    // Обработчики для загрузки истории и навигации по страницам
    document.getElementById('loadHistory').addEventListener('click', () => {
        page = 1; // Сброс на первую страницу
        loadHistory(page); // Загрузка истории
    });

    document.getElementById('prevPage').addEventListener('click', () => {
        if (page > 1) {
            page--; // Переход на предыдущую страницу
            loadHistory(page);
        }
    });

    document.getElementById('nextPage').addEventListener('click', () => {
        if (page < totalPages) {
            page++; // Переход на следующую страницу
            loadHistory(page);
        }
    });

    document.getElementById('clearLog').addEventListener('click', () => {
        alertLogDiv.innerHTML = ''; // Очистка лога
        fetch('http://localhost:8080/api/alerts/clear', { method: 'POST' }); // Очистка лога на сервере
    });

    /**
     * Запуск WebSocket для получения сырых и физических данных
     */
    function startDataWS() {
        wsData = new WebSocket('ws://localhost:8080/api/monitor/ws/data');
        wsData.onmessage = event => {
            const payload = JSON.parse(event.data);
            console.log(payload)
            updateRawTable(payload.raw); // Обновление таблицы сырых данных
            updatePhysTable(payload.physical); // Обновление таблицы физических данных
        };
    }

    /**
     * Остановка WebSocket для данных
     */
    function stopDataWS() {
        if (wsData) wsData.close(); // Закрытие WebSocket, если он существует
    }

    /**
     * Запуск WebSocket для получения алертов
     */
    function startAlertsWS() {
        wsAlerts = new WebSocket('ws://localhost:8080/api/alerts/ws/alerts');
        wsAlerts.onmessage = event => {
            const alert = JSON.parse(event.data);
            appendAlert(alert); // Добавление алерта в лог
        };
    }

    /**
     * Остановка WebSocket для данных
     */
    function stopAlertsWS() {
        if (wsAlerts) wsAlerts.close(); // Закрытие WebSocket, если он существует
    }

    /**
     * Обновление таблицы сырых данных
     * @param {Array} data - Массив объектов с полями device, m1..b3
     */
    function updateRawTable(data) {
        rawTableBody.innerHTML = ''; // Очистка таблицы
        data.forEach(item => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${item.device}</td>
                <td>${item.m1}</td><td>${item.m2}</td><td>${item.m3}</td>
                <td>${item.m4}</td><td>${item.m5}</td><td>${item.m6}</td>
                <td>${item.t1}</td><td>${item.t2}</td><td>${item.t3}</td>
                <td>${item.t4}</td><td>${item.t5}</td><td>${item.t6}</td>
                <td>${item.l1}</td><td>${item.l2}</td><td>${item.l3}</td>
                <td>${item.l4}</td><td>${item.l5}</td><td>${item.l6}</td>
                <td>${item.X}</td><td>${item.Y}</td><td>${item.T}</td>
                <td>${item.G}</td><td>${item.V}</td>
                <td>${item.code}</td><td>${item.p}</td>
                <td>${item.b1}</td><td>${item.b2}</td><td>${item.b3}</td>
            `;
            rawTableBody.appendChild(tr); // Добавление строки в таблицу
        });
    }

    /**
     * Обновление таблицы физических данных с подсветкой статусов
     * @param {Array} data - Массив объектов с полями theta1..b3 и статусами status_theta1..
     */
    function updatePhysTable(data) {
        physTableBody.innerHTML = ''; // Очистка таблицы
        data.forEach(item => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${item.device}</td>
                <td>${item.theta1}</td>
                <td>${item.theta2}</td>
                <td>${item.theta3}</td>
                <td>${item.theta4}</td>
                <td>${item.theta5}</td>
                <td>${item.theta6}</td>
                <td>${item.T1}</td>
                <td>${item.T2}</td>
                <td>${item.T3}</td>
                <td>${item.T4}</td>
                <td>${item.T5}</td>
                <td>${item.T6}</td>
                <td>${item.L1}</td>
                <td>${item.L2}</td>
                <td>${item.L3}</td>
                <td>${item.L4}</td>
                <td>${item.L5}</td>
                <td>${item.L6}</td>
                <td>${item.X}</td>
                <td>${item.Y}</td>
                <td>${item.Tpos}</td>
                <td>${item.G}</td>
                <td>${item.V}</td>
                <td>${item.code}</td>
                <td>${item.p}</td>
                <td>${item.b1}</td>
                <td>${item.b2}</td>
                <td>${item.b3}</td>
            `;
            physTableBody.appendChild(tr); // Добавление строки в таблицу
        });
    }

    /**
     * Добавление записи критического события в лог
     * @param {Object} alert - Объект алерта: { timestamp, device, param, value, threshold }
     */
    function appendAlert(alert) {
        const div = document.createElement('div');
        // div.textContent = `${alert.timestamp} - ${alert.device}: ${alert.param}=${alert.value}, порог=${alert.threshold}`;
        div.textContent = `${alert.level}:${alert.timestamp} ${alert.message}`
        alertLogDiv.prepend(div); // Добавление записи в начало лога
    }

    /**
     * Построение графика по выбранным параметрам и временному диапазону
     */
    function buildChart() {
  const deviceId = document.getElementById('deviceSelect').value;
  const from     = document.getElementById('fromTime').value;
  const to       = document.getElementById('toTime').value;
  const ms    = document.getElementById('msSelect').value; // один параметр

  // Собираем URL с 4 параметрами: from, to, deviceId и ms
  const url = `http://localhost:8080/api/data?` +
    `deviceId=${encodeURIComponent(deviceId)}` +
    `&from=${encodeURIComponent(from)}` +
    `&to=${encodeURIComponent(to)}` +
    `&ms=${encodeURIComponent(ms)}`;

    fetch(url)
      .then(res => res.json())
      .then(response => {
        const ctx = document.getElementById('dataChart').getContext('2d');
        if (window.chart) window.chart.destroy();

        // Ожидаем формат:
        // {
        //   timestamps: [...],
        //   серии: {
        //     theta1: [...], theta2: [...], ...
        //   }
        // }
        const labels = response.timestamps;
        const series = response.series; // объект с нужными ключами

        // Преобразуем в массив datasets для Chart.js
        const datasets = Object.keys(series).map(key => ({
          label: key,
          data: series[key]
        }));

        window.chart = new Chart(ctx, {
          type: 'line',
          data: { labels, datasets },
          options: { responsive: true, scales: { x: { type: 'time' } } }
        });
      });
      }
      document.getElementById('buildChart').addEventListener('click', buildChart);

    /**
     * Загрузка истории данных по временному диапазону и номеру страницы
     * @param {number} pageNum - Номер страницы
     */
    function loadHistory(pageNum) {
        const from = document.getElementById('histFrom').value;
        const to = document.getElementById('histTo').value;
        fetch(`/api/data/history?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&page=${pageNum}`)
            .then(res => res.json())
            .then(result => {
                const tbody = document.querySelector('#historyTable tbody');
                tbody.innerHTML = ''; // Очистка таблицы
                result.entries.forEach(r => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td>${r.timestamp}</td>
                        <td>${r.device}</td>
                        <td>${r.raw}</td>
                        <td>${r.physical}</td>
                        <td>${r.status}</td>
                    `;
                    tbody.appendChild(tr); // Добавление строки в таблицу
                });
                page = result.page; // Обновление текущей страницы
                totalPages = result.totalPages; // Обновление общего количества страниц
                document.getElementById('pageInfo').textContent = `Стр. ${result.page} из ${result.totalPages}`;
                // Управление активностью кнопок навигации
                document.getElementById('prevPage').disabled = page <= 1;
                document.getElementById('nextPage').disabled = page >= totalPages;
            });
    }

    // Обработчик для смены роли на операторский интерфейс
    document.getElementById('switchRole').addEventListener('click', () => {
        window.location.href = '../operator/index.html'; // Перенаправление на интерфейс оператора
    });
});