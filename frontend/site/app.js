const REFRESH_MS = 2000;
const STATE_NAMES = {
    RUNNING: 'Работает',
    STOPPED: 'Остановлен',
    ALARM: 'Авария'
};

// карточка создаётся один раз, потом в ней только меняются значения,
// иначе поле ввода скорости сбрасывалось бы при каждом обновлении
function createCard(m) {
    const card = document.createElement('div');
    card.className = 'machine';
    card.id = 'machine-' + m.id;
    card.innerHTML = `
        <div class="machine-head">
            <div>
                <div class="machine-name">${m.name}</div>
                <div class="machine-type">${m.type}</div>
            </div>
            <span class="state" id="state-${m.id}"></span>
        </div>
        <div class="param"><span>Температура</span><b id="temp-${m.id}"></b></div>
        <div class="bar"><div id="bar-${m.id}"></div></div>
        <div class="param"><span>Скорость</span><b id="speed-${m.id}"></b></div>
        <div class="param"><span>Выпущено деталей</span><b id="produced-${m.id}"></b></div>
        <div class="buttons">
            <button class="btn-start" onclick="sendCommand(${m.id}, 'start', 0)">Пуск</button>
            <button class="btn-stop" onclick="sendCommand(${m.id}, 'stop', 0)">Стоп</button>
            <button class="btn-reset" onclick="sendCommand(${m.id}, 'reset', 0)">Сброс аварии</button>
        </div>
        <div class="speed-row">
            <input type="number" id="speed-input-${m.id}" min="0" max="100" value="${m.speed}">
            <span>%</span>
            <button onclick="setSpeed(${m.id})">Задать скорость</button>
        </div>
        <div class="updated" id="updated-${m.id}"></div>`;
    document.getElementById('machines').appendChild(card);
}

function updateCard(m) {
    const card = document.getElementById('machine-' + m.id);
    card.className = 'machine ' + m.state.toLowerCase();

    const state = document.getElementById('state-' + m.id);
    state.textContent = STATE_NAMES[m.state] || m.state;
    state.className = 'state ' + m.state.toLowerCase();

    document.getElementById('temp-' + m.id).textContent = m.temperature.toFixed(1) + ' °C';
    document.getElementById('speed-' + m.id).textContent = m.speed + ' %';
    document.getElementById('produced-' + m.id).textContent = m.produced;
    document.getElementById('updated-' + m.id).textContent =
        'Данные от ' + new Date(m.updatedAt).toLocaleTimeString('ru-RU');

    const bar = document.getElementById('bar-' + m.id);
    bar.style.width = Math.min(m.temperature, 100) + '%';
    if (m.temperature > 90) {
        bar.className = 'hot';
    } else if (m.temperature > 75) {
        bar.className = 'warm';
    } else {
        bar.className = '';
    }
}

async function loadMachines() {
    const response = await fetch('/api/machines');
    const machines = await response.json();
    let running = 0;
    let stopped = 0;
    let alarm = 0;
    let produced = 0;

    for (const m of machines) {
        if (!document.getElementById('machine-' + m.id)) {
            createCard(m);
        }
        updateCard(m);
        if (m.state === 'RUNNING') running++;
        if (m.state === 'STOPPED') stopped++;
        if (m.state === 'ALARM') alarm++;
        produced += m.produced;
    }
    document.getElementById('sum-running').textContent = running;
    document.getElementById('sum-stopped').textContent = stopped;
    document.getElementById('sum-alarm').textContent = alarm;
    document.getElementById('sum-produced').textContent = produced;
}

function addCell(row, text, className) {
    const cell = document.createElement('td');
    cell.textContent = text;
    if (className) {
        cell.className = className;
    }
    row.appendChild(cell);
}

async function loadEvents() {
    const response = await fetch('/api/events');
    const events = await response.json();
    const tbody = document.getElementById('events');
    tbody.innerHTML = '';
    for (const e of events) {
        const row = document.createElement('tr');
        addCell(row, new Date(e.time).toLocaleTimeString('ru-RU'));
        addCell(row, e.machine);
        addCell(row, e.source, e.source === 'оператор' ? 'src-operator' : 'src-machine');
        addCell(row, e.message);
        tbody.appendChild(row);
    }
}

async function sendCommand(id, action, value) {
    try {
        const response = await fetch('/api/machines/' + id + '/command', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: action, value: value })
        });
        const result = await response.json();
        showMessage(result.result || result.error, !response.ok);
    } catch (err) {
        showMessage('Не удалось отправить команду', true);
    }
    setTimeout(refresh, 500);
}

function setSpeed(id) {
    const value = parseInt(document.getElementById('speed-input-' + id).value);
    if (isNaN(value) || value < 0 || value > 100) {
        showMessage('Скорость должна быть от 0 до 100 %', true);
        return;
    }
    sendCommand(id, 'speed', value);
}

function showMessage(text, isError) {
    const box = document.getElementById('message');
    box.textContent = text;
    box.className = isError ? 'message error' : 'message ok';
    setTimeout(() => { box.className = 'message'; }, 3000);
}

async function refresh() {
    const connection = document.getElementById('connection');
    try {
        await loadMachines();
        await loadEvents();
        connection.textContent = 'Связь с сервером есть';
        connection.className = 'connection online';
    } catch (err) {
        connection.textContent = 'Нет связи с сервером';
        connection.className = 'connection offline';
    }
}

refresh();
setInterval(refresh, REFRESH_MS);
