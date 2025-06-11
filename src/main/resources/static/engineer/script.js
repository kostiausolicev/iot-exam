document.addEventListener('DOMContentLoaded', () => {

    const deviceSelect = document.getElementById('deviceSelect');
    const fromInput = document.getElementById('fromTime');
    const toInput = document.getElementById('toTime');
    const msSelect = document.getElementById('msSelect');
    const buildBtn = document.getElementById('buildChart');
    const ctx = document.getElementById('dataChart').getContext('2d');
    let chartInstance = null;

    const toggleCollect = document.getElementById('toggleCollect');
    const toggleSave = document.getElementById('toggleSave');
    const updateFreq = document.getElementById('updateFreq');
    const saveThresholdsBtn = document.getElementById('saveThresholds');
    const rawTableBody = document.querySelector('#rawDataTable tbody');
    const physTableBody = document.querySelector('#physicalDataTable tbody');
    const alertLogDiv = document.getElementById('alertLog');

    let wsData, wsAlerts;
    let page = 1,
        totalPages = 1;

    const params = [
        'm1', 'm2', 'm3', 'm4', 'm5', 'm6',
        't1', 't2', 't3', 't4', 't5', 't6',
        'l1', 'l2', 'l3', 'l4', 'l5', 'l6',
        'X', 'Y', 'T', 'G', 'V'
    ];


    fetch('http://localhost:8080/api/devices')
        .then(res => res.json())
        .then(devices => {
            deviceSelect.innerHTML = '';
            devices.forEach(d => {
                const opt = document.createElement('option');
                opt.value = d.id;
                opt.textContent = d.name;
                deviceSelect.appendChild(opt);
            });
        })
        .catch(err => console.error('Не удалось загрузить устройства:', err));


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
    renderThresholdInputs();

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
    saveThresholdsBtn.addEventListener('click', saveThresholds);
    loadThresholds();

    toggleCollect.addEventListener('change', () => {
        toggleCollect.checked ? startDataWS() : stopDataWS();
        fetch('http://localhost:8080/api/config', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ collect: toggleCollect.checked })
        });
    });
    toggleSave.addEventListener('change', () => {
        fetch('http://localhost:8080/api/config', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ save: toggleSave.checked })
        });
    });
    updateFreq.addEventListener('change', () => {
        fetch('http://localhost:8080/api/config', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ frequency: +updateFreq.value })
        });
    });


    function startDataWS() {
        wsData = new WebSocket('ws://localhost:8080/api/monitor/ws/data');
        wsData.onmessage = e => {
            const { raw, physical } = JSON.parse(e.data);
            updateRawTable(raw);
            updatePhysTable(physical);
        };
    }

    function stopDataWS() {
        if (wsData) wsData.close();
    }


    function startAlertsWS() {
        wsAlerts = new WebSocket('ws://localhost:8080/api/alerts/ws/alerts');
        wsAlerts.onmessage = e => {
            const a = JSON.parse(e.data);
            const div = document.createElement('div');
            div.textContent = `${a.level}: ${a.timestamp} ${a.message}`;
            alertLogDiv.prepend(div);
        };
    }

    function stopAlertsWS() {
        if (wsAlerts) wsAlerts.close();
    }


    function updateRawTable(data) {
        rawTableBody.innerHTML = '';
        data.forEach(it => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
        <td>${it.device}</td>
        <td>${it.m1}</td><td>${it.m2}</td><td>${it.m3}</td>
        <td>${it.m4}</td><td>${it.m5}</td><td>${it.m6}</td>
        <td>${it.t1}</td><td>${it.t2}</td><td>${it.t3}</td>
        <td>${it.t4}</td><td>${it.t5}</td><td>${it.t6}</td>
        <td>${it.l1}</td><td>${it.l2}</td><td>${it.l3}</td>
        <td>${it.l4}</td><td>${it.l5}</td><td>${it.l6}</td>
        <td>${it.X}</td><td>${it.Y}</td><td>${it.T}</td>
        <td>${it.G}</td><td>${it.V}</td>
        <td>${it.code}</td><td>${it.p}</td>
        <td>${it.b1}</td><td>${it.b2}</td><td>${it.b3}</td>
      `;
            rawTableBody.appendChild(tr);
        });
    }

    function updatePhysTable(data) {
        physTableBody.innerHTML = '';
        data.forEach(it => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
        <td>${it.device}</td>
        <td>${it.theta1}</td><td>${it.theta2}</td>
        <td>${it.theta3}</td><td>${it.theta4}</td>
        <td>${it.theta5}</td><td>${it.theta6}</td>
        <td>${it.T1}</td><td>${it.T2}</td>
        <td>${it.T3}</td><td>${it.T4}</td>
        <td>${it.T5}</td><td>${it.T6}</td>
        <td>${it.L1}</td><td>${it.L2}</td>
        <td>${it.L3}</td><td>${it.L4}</td>
        <td>${it.L5}</td><td>${it.L6}</td>
        <td>${it.X}</td><td>${it.Y}</td><td>${it.Tpos}</td>
        <td>${it.G}</td><td>${it.V}</td>
        <td>${it.code}</td><td>${it.p}</td>
        <td>${it.b1}</td><td>${it.b2}</td><td>${it.b3}</td>
      `;
            physTableBody.appendChild(tr);
        });
    }

    function loadHistory(pg) {
        const from = document.getElementById('histFrom').value;
        const to = document.getElementById('histTo').value;
        fetch(`http://localhost:8080/api/data/history?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&page=${pg}`)
            .then(r => r.json())
            .then(res => {
                const tb = document.querySelector('#historyTable tbody');
                tb.innerHTML = '';
                res.entries.forEach(r => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
            <td>${r.timestamp}</td>
            <td>${r.device}</td>
            <td>${r.raw}</td>
            <td>${r.physical}</td>
            <td>${r.status}</td>
          `;
                    tb.appendChild(tr);
                });
                page = res.page;
                totalPages = res.totalPages;
                document.getElementById('pageInfo').textContent = `Стр. ${page} из ${totalPages}`;
                document.getElementById('prevPage').disabled = page <= 1;
                document.getElementById('nextPage').disabled = page >= totalPages;
            });
    }
    document.getElementById('loadHistory').addEventListener('click', () => {
        page = 1;
        loadHistory(1);
    });
    document.getElementById('prevPage').addEventListener('click', () => { if (page > 1) loadHistory(--page); });
    document.getElementById('nextPage').addEventListener('click', () => { if (page < totalPages) loadHistory(++page); });


let chart; // глобальная переменная для графика

function buildChart() {
  const deviceId = deviceSelect.value;
  const from     = fromInput.value;
  const to       = toInput.value;
  const ms       = msSelect.value;

  if (!deviceId || !from || !to || !ms) {
    alert('Выберите устройство, параметр и укажите диапазон по времени');
    return;
  }

  const url = `http://localhost:8080/api/data?`
            + `&from=${encodeURIComponent(from)}`
            + `&to=${encodeURIComponent(to)}`
            + `&ms=${encodeURIComponent(ms)}`
            + `&deviceId=${encodeURIComponent(deviceId)}`;

  fetch(url)
    .then(res => res.json())
    .then(data => {
      // Универсальная обёртка: если массив — берём первый элемент, если объект — оставляем как есть
      const parsed = Array.isArray(data) ? data[0] : data;

      console.log(data)

      if (!parsed || !parsed.timestamps || !parsed.X || !parsed.Y || !parsed.T) {
        alert('Пустой или некорректный ответ от сервера');
        return;
      }

      const timestamps = parsed.timestamps.map(ts => new Date(ts).toLocaleTimeString());
      const xValues = parsed.X;
      const yValues = parsed.Y;
      const tValues = parsed.T;

      const ctx = document.getElementById('dataChart').getContext('2d');

      if (chart) chart.destroy();

      chart = new Chart(ctx, {
        type: 'line',
        data: {
          labels: timestamps,
          datasets: [
            {
              label: 'X',
              data: xValues,
              borderColor: 'rgba(255, 99, 132, 1)',
              backgroundColor: 'rgba(255, 99, 132, 0.2)',
              fill: false,
              tension: 0.1
            },
            {
              label: 'Y',
              data: yValues,
              borderColor: 'rgba(54, 162, 235, 1)',
              backgroundColor: 'rgba(54, 162, 235, 0.2)',
              fill: false,
              tension: 0.1
            },
            {
              label: 'T',
              data: tValues,
              borderColor: 'rgba(255, 206, 86, 1)',
              backgroundColor: 'rgba(255, 206, 86, 0.2)',
              fill: false,
              tension: 0.1
            }
          ]
        },
        options: {
          responsive: true,
          scales: {
            x: {
              title: {
                display: true,
                text: 'Время'
              }
            },
            y: {
              title: {
                display: true,
                text: 'Значение'
              }
            }
          },
          interaction: {
            mode: 'index',
            intersect: false
          },
          plugins: {
            title: {
              display: true,
              text: 'Графики X, Y и T по времени'
            },
            tooltip: {
              mode: 'index',
              intersect: false
            }
          }
        }
      });
    })
    .catch(error => {
      console.error('Ошибка при получении данных:', error);
      alert('Не удалось получить данные с сервера');
    });
}

    buildBtn.addEventListener('click', buildChart);

    document.getElementById('switchRole').addEventListener('click', () => {
        window.location.href = '../operator/index.html';
    });
});