$(".custom-button").on("click", function() {
        // Убираем класс "active" у всех кнопок
        $(".custom-button").removeClass("active-button");
        // Добавляем класс "active" к нажатой кнопке
        $(this).addClass("active-button");
        });

$(".submit-button").on("click", function() {
        let xValue = $("input[name='xValue']:checked").val() || null;
        let yValue = $("input[name='yValue']").val() || null;
        let rValue = $(".active-button").val() || null;

        // Создаем JSON-объект
        let json = {
            x: xValue,
            y: yValue,
            r: rValue
        };

        // Дополнительная проверка перед отправкой
        if (typeof json.x === 'undefined' ||
            typeof json.y === 'undefined' ||
            typeof json.r === 'undefined') {
            console.error('Не все значения были получены');

        console.log(json.x, json.y, json.r)}
        else {
        startTime = new Date().getTime();

        fetch("/httpd/fcgi/web.jar?" + new URLSearchParams(json), {
        method: "GET",
        headers: {
        "Content-Type": "application/json"
        }
        })
        .then(response => {
         if (!response.ok) {
                throw new Error("Network response was not ok");
            }
            console.log('Статус ответа:', response.status);
            console.log('Заголовки:', response.headers);
            return response.json();
        })

        .then(data => {
        let odz;
        if (response.result) {
        odz = "Попадание";
        } else {
        odz = "Мимо";
        }

        // Время выполнения скрипта
        let endTime = new Date();
        let executionTime = (endTime - startTime) + " ms";

        // Текущее время
        let currentTime = new Date().toLocaleString();

        // Добавляем строку в таблицу
        let newRow =`
        <tr>
            <td>${json.x}</td>
            <td>${json.y}</td>
            <td>${json.r}</td>
            <td>${odz}</td>
            <td>${currentTime}</td>
            <td>${executionTime}</td>
        </tr>`;

        document.querySelector("#resultTable tbody").insertAdjacentHTML('beforeend', newRow);
        })
        .catch(error => {
        alert("Ошибка при отправке данных: " + error.message);
        });
        }
        });
