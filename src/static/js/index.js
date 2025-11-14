// Обработчики для кнопок выбора R
document.addEventListener('DOMContentLoaded', function() {
    console.log('Страница загружена');
    
    // Назначаем обработчики для кнопок R
    const rButtons = document.querySelectorAll('.custom-button');
    rButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Убираем класс "active-button" у всех кнопок
            rButtons.forEach(btn => btn.classList.remove('active-button'));
            // Добавляем класс "active-button" к нажатой кнопке
            this.classList.add('active-button');
        });
    });
    
    // Назначаем обработчик для кнопки отправки
    const submitButton = document.querySelector('.submit-button');
    if (submitButton) {
        submitButton.addEventListener('click', submitForm);
    } else {
        console.error('Кнопка отправки не найдена');
    }
});

let startTime;

function submitForm() {
    console.log('Обработка формы...');
    
    // Получаем значения
    let xValue = getSelectedXValue();
    let yValue = getYValue();
    let rValue = getSelectedRValue();
    
    // Создаем объект с данными
    let data = {
        x: xValue,
        y: yValue,
        r: rValue
    };
    
    console.log('Данные для отправки:', data);
    
    // Проверяем, что все значения есть
    if (data.x === null || data.y === null || data.r === null) {
        console.error('Не все значения были получены');
        alert('Пожалуйста, заполните все поля:\n- Выберите X\n- Введите Y\n- Выберите R');
        return;
    }
    
    // Дополнительная валидация чисел
    if (!validateNumbers(data.x, data.y, data.r)) {
        return;
    }
    
    // Запускаем таймер
    startTime = new Date().getTime();
    
    // Отправляем запрос на сервер
    sendRequest(data);
}

function getSelectedXValue() {
    // Ищем выбранный radio button для X
    const selectedX = document.querySelector('input[name="xValue"]:checked');
    return selectedX ? selectedX.value : null;
}

function getYValue() {
    // Получаем значение Y из input
    const yInput = document.querySelector('input[name="yValue"]');
    if (yInput && yInput.value.trim() !== '') {
        return parseFloat(yInput.value);
    }
    return null;
}

function getSelectedRValue() {
    // Ищем активную кнопку R
    const activeRButton = document.querySelector('.custom-button.active-button');
    return activeRButton ? activeRButton.value : null;
}

function validateNumbers(x, y, r) {
    // Проверяем X (должно быть число)
    const xNum = parseFloat(x);
    if (isNaN(xNum) || xNum < -4 || xNum > 4) {
        alert('X должно быть числом от -4 до 4');
        return false;
    }
    
    // Проверяем Y (должно быть число от -3 до 5)
    if (isNaN(y) || y < -3 || y > 5) {
        alert('Y должно быть числом от -3 до 5');
        return false;
    }
    
    // Проверяем R (должно быть число от 1 до 5)
    const rNum = parseFloat(r);
        const allowedRValues = [1, 1.5, 2, 2.5, 3];

        if (isNaN(rNum) || !allowedRValues.includes(rNum)) {
            alert('R должно быть одним из значений: 1, 1.5, 2, 2.5, 3');
            return false;
        }

        return true;
}

function sendRequest(data) {
    console.log('Отправка запроса на сервер...');
    
    // Формируем URL с параметрами
    const params = new URLSearchParams();
    params.append('x', data.x);
    params.append('y', data.y);
    params.append('r', data.r);
    
    // ИСПРАВИТЬ ЭТУ СТРОКУ
    const url = '/check?' + params.toString();
    console.log('URL запроса:', url);
    
    fetch(url, {
        method: "GET",
        headers: {
            "Content-Type": "application/json"
        }
    })
    .then(response => {
        console.log('Получен ответ, статус:', response.status);
        
        if (!response.ok) {
            throw new Error("Network response was not ok: " + response.status);
        }
        return response.json();
    })
    .then(data => {
        console.log('Данные от сервера:', data);
        processServerResponse(data);
    })
    .catch(error => {
        console.error('Ошибка при отправке данных:', error);
        alert("Ошибка при отправке данных: " + error.message);
    });
}

function processServerResponse(response) {
    // Определяем результат
    let resultText;
    if (response.result) {
        resultText = "Попадание";
    } else {
        resultText = "Мимо";
    }
    
    // Время выполнения скрипта
    let endTime = new Date().getTime();
    let executionTime = (endTime - startTime) + " ms";
    
    // Текущее время
    let currentTime = new Date().toLocaleString();
    
    // Данные из формы (берем из response или сохраняем отдельно)
    const formData = {
        x: response.x || document.querySelector('input[name="xValue"]:checked').value,
        y: response.y || document.querySelector('input[name="yValue"]').value,
        r: response.r || document.querySelector('.custom-button.active-button').value
    };
    
    // Добавляем строку в таблицу
    addTableRow(formData, resultText, currentTime, executionTime);
}

function addTableRow(data, result, currentTime, executionTime) {
    const tableBody = document.querySelector("#resultTable tbody");
    
    if (!tableBody) {
        console.error('Таблица результатов не найдена');
        // Создаем таблицу если её нет
        createResultsTable();
        return;
    }
    
    // Создаем новую строку
    const newRow = document.createElement('tr');
    newRow.innerHTML = `
        <td>${data.x}</td>
        <td>${data.y}</td>
        <td>${data.r}</td>
        <td>${result}</td>
        <td>${currentTime}</td>
        <td>${executionTime}</td>
    `;
    
    // Добавляем строку в начало таблицы
    tableBody.insertBefore(newRow, tableBody.firstChild);
    
    console.log('Результат добавлен в таблицу');
}

function createResultsTable() {
    console.log('Создаем таблицу результатов...');
    // Если таблицы нет, можно создать её динамически
    const container = document.getElementById('historyContainer') || document.body;
    
    const tableHTML = `
        <table id="resultTable">
            <thead>
                <tr>
                    <th>X</th>
                    <th>Y</th>
                    <th>R</th>
                    <th>Результат</th>
                    <th>Текущее время</th>
                    <th>Время выполнения</th>
                </tr>
            </thead>
            <tbody></tbody>
        </table>
    `;
    
    container.innerHTML = tableHTML;
}