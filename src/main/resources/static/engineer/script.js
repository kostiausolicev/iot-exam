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
        container.innerHTML = '';
        params.forEach(p => {
            ['warn', 'crit'].forEach(type => {
                const label = document.createElement('label');
                label.textContent = `${type==='warn'?'Warning':'Critical'} ${p}`;
                const min = document.createElement('input');
                min.type = 'number';
                min.id = `${type}_${p}_min`;
                min.placeholder = 'Min';
                const max = document.createElement('input');
                max.type = 'number';
                max.id = `${type}_${p}_max`;
                max.placeholder = 'Max';
                container.appendChild(label);
                container.appendChild(min);
                container.appendChild(max);
            });
        });
    }
    renderThresholdInputs();

    function loadThresholds() {
        fetch('http://localhost:8080/api/thresholds')
            .then(res => res.json())
            .then(data => {
                params.forEach(p => {
                    const t = data[p] || {};
                    document.getElementById(`warn_${p}_min`).value = t.warning?.min ?? '';
                    document.getElementById(`warn_${p}_max`).value = t.warning?.max ?? '';
                    document.getElementById(`crit_${p}_min`).value = t.critical?.min ?? '';
                    document.getElementById(`crit_${p}_max`).value = t.critical?.max ?? '';
                });
            });
    }

    function saveThresholds() {
        const payload = {};
        params.forEach(p => {
            payload[p] = {
                warning: {
                    min: +document.getElementById(`warn_${p}_min`).value,
                    max: +document.getElementById(`warn_${p}_max`).value
                },
                critical: {
                    min: +document.getElementById(`crit_${p}_min`).value,
                    max: +document.getElementById(`crit_${p}_max`).value
                }
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


    fetch('http://localhost:8080/api/config')
        .then(res => res.json())
        .then(cfg => {
            toggleCollect.checked = cfg.collect;
            toggleSave.checked = cfg.save;
            updateFreq.value = cfg.frequency;
            if (cfg.collect) startDataWS();
            startAlertsWS();
        });

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


function buildChart() {
  const deviceId = deviceSelect.value;
  const from     = fromInput.value;
  const to       = toInput.value;
  const ms       = msSelect.value;

  if (!deviceId || !from || !to || !ms) {
    alert('Выберите устройство, параметр и укажите диапазон по времени');
    return;
  }

  const url = `http://localhost:8080/api/data?deviceId=${encodeURIComponent(deviceId)}`
            + `&from=${encodeURIComponent(from)}`
            + `&to=${encodeURIComponent(to)}`
            + `&ms=${encodeURIComponent(ms)}`;

  fetch(url)
    .then(r => r.json())
    .then(resp => {
      // Формируем данные для графика — для каждого параметра массив {x: timestamp, y: value}
      const datasets = ['X', 'Y', 'T'].map(key => ({
        label: key,
        data: resp.timestamps.map((ts, i) => ({
          x: ts,
          y: resp[key][i]
        })),
        borderColor: key === 'X' ? 'red' : key === 'Y' ? 'green' : 'blue',
        fill: false,
        tension: 0.1
      }));

      if (chartInstance) chartInstance.destroy();
      chartInstance = new Chart(ctx, {
        type: 'line',
        data: { datasets },
        options: {
          responsive: true,
          scales: {
            x: {
              type: 'time',
              time: {
                parser: 'isoDateTime',
                tooltipFormat: 'PPpp',
                unit: 'second'
              },
              title: { display: true, text: 'Время' }
            },
            y: {
              title: { display: true, text: 'Значение' }
            }
          },
          plugins: {
            legend: {
              display: true,
              position: 'top'
            }
          }
        }
      });
    })
    .catch(err => {
      console.error('Ошибка при загрузке графика:', err);
      alert('Не удалось загрузить данные графика');
    });
}

    buildBtn.addEventListener('click', buildChart);

    document.getElementById('switchRole').addEventListener('click', () => {
        window.location.href = '../operator/index.html';
    });
});