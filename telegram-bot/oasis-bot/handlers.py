from aiogram import Router, F
from aiogram.types import Message, CallbackQuery
from aiogram.filters import Command
from aiogram.fsm.context import FSMContext

from config import config
from keyboards import main_menu, order_menu, payment_menu, back_menu
from states import OrderState, AdminState
from database import db

router = Router()

# Услуги с фиксированной ценой
FIXED_PRICES = {
    "Скин": 249,
    "Решейд скина": 149,
    "Тотем по скину": 99,
}

# Услуги, требующие расчёта администратором
CALC_SERVICES = {
    "Превью для видео",
    "Монтаж видео",
    "Мод / плагин",
    "Сборка на заказ",
    "Постройка на заказ",
    "Minecraft-сайт",
}

# Соответствие callback_data -> название услуги
SERVICE_MAP = {
    "service_skin": "Скин",
    "service_reshade": "Решейд скина",
    "service_totem": "Тотем по скину",
    "service_preview": "Превью для видео",
    "service_editing": "Монтаж видео",
    "service_mod": "Мод / плагин",
    "service_build": "Сборка на заказ",
    "service_construction": "Постройка на заказ",
    "service_website": "Minecraft-сайт",
}


def welcome_text() -> str:
    return (
        f"Добро пожаловать в {config.STUDIO_NAME}!\n\n"
        f"Выберите услугу ниже.\n"
        f"Наш Telegram-канал: {config.CHANNEL_URL}"
    )


def payment_text(service: str, price: int) -> str:
    return (
        f"Оплата заказа\n\n"
        f"Услуга: {service}\n"
        f"Цена: {price} ₽\n\n"
        f"Банк: {config.BANK_NAME}\n"
        f"Номер: {config.PAYMENT_NUMBER}\n"
        f"Получатель: {config.RECIPIENT_NAME}\n\n"
        f"После оплаты отправьте сюда скрин чека."
    )


# ==================== СТАРТ И ГЛАВНОЕ МЕНЮ ====================

@router.message(Command("start"))
async def start(message: Message, state: FSMContext):
    await state.clear()
    await message.answer(welcome_text(), reply_markup=main_menu())


@router.callback_query(F.data == "back_to_menu")
async def back_to_menu(callback: CallbackQuery, state: FSMContext):
    await state.clear()
    await callback.message.edit_text(welcome_text(), reply_markup=main_menu())
    await callback.answer()


# ==================== ВЫБОР УСЛУГИ ====================

@router.callback_query(F.data.startswith("service_"))
async def choose_service(callback: CallbackQuery, state: FSMContext):
    service = SERVICE_MAP.get(callback.data)
    if not service:
        await callback.answer("Неизвестная услуга", show_alert=True)
        return

    await state.clear()
    await state.update_data(service=service, description="", photos=[], documents=[], skin_file=None)

    if service == "Решейд скина":
        text = (
            "Отправьте файл вашего скина (.png).\n"
            "Фото не подходит — нужен именно файл."
        )
        await state.set_state(OrderState.waiting_skin_file)
        await callback.message.edit_text(text, reply_markup=order_menu())
        await callback.answer()
        return

    if service == "Тотем по скину":
        text = (
            "Отправьте файл вашего скина (.png).\n"
            "После этого напишите пожелания."
        )
        await state.set_state(OrderState.waiting_skin_file)
        await callback.message.edit_text(text, reply_markup=order_menu())
        await callback.answer()
        return

    if service == "Скин":
        text = (
            "Опишите ваш скин.\n\n"
            "Напишите:\n"
            "• стиль\n"
            "• цвета\n"
            "• детали\n\n"
            "Если хотите, можете приложить фотографии или файлы с референсами."
        )
    else:
        text = (
            "Опишите заказ максимально подробно.\n\n"
            "Можно приложить фотографии, файлы и референсы.\n"
            "После отправки нажмите «Готово»."
        )

    await state.set_state(OrderState.waiting_description)
    await callback.message.edit_text(text, reply_markup=order_menu())
    await callback.answer()


# ==================== ФАЙЛ СКИНА (решейд / тотем) ====================

@router.message(OrderState.waiting_skin_file, F.document)
async def receive_skin_file(message: Message, state: FSMContext):
    doc = message.document
    if not doc.file_name or not doc.file_name.lower().endswith(".png"):
        await message.answer("Нужен именно файл .png с вашим скином. Попробуйте ещё раз.")
        return

    await state.update_data(skin_file=doc.file_id)
    await state.set_state(OrderState.waiting_description)
    await message.answer(
        "Файл получен!\nТеперь напишите текстовое описание (пожелания к заказу).",
        reply_markup=order_menu()
    )


@router.message(OrderState.waiting_skin_file)
async def receive_skin_file_invalid(message: Message):
    await message.answer(
        "Нужен именно файл .png с вашим скином — отправьте его как документ, не как фото."
    )


# ==================== ОПИСАНИЕ И ФАЙЛЫ ЗАКАЗА ====================

@router.message(OrderState.waiting_description, F.text)
async def receive_description(message: Message, state: FSMContext):
    data = await state.get_data()
    desc = data.get("description", "")
    desc = (desc + "\n" + message.text).strip()
    await state.update_data(description=desc)
    await message.answer(
        "Описание сохранено!\nЕсли всё отправили — нажмите «Готово».",
        reply_markup=order_menu()
    )


@router.message(OrderState.waiting_description, F.photo | F.document)
async def receive_files(message: Message, state: FSMContext):
    data = await state.get_data()
    photos = data.get("photos", [])
    documents = data.get("documents", [])

    if message.photo:
        photos.append(message.photo[-1].file_id)
    elif message.document:
        documents.append(message.document.file_id)

    await state.update_data(photos=photos, documents=documents)
    await message.answer(
        "Файл сохранён.\nНе забудьте отправить текстовое описание, затем нажмите «Готово».",
        reply_markup=order_menu()
    )


# ==================== ЗАВЕРШЕНИЕ ОФОРМЛЕНИЯ ЗАКАЗА ====================

@router.callback_query(F.data == "order_done")
async def order_done(callback: CallbackQuery, state: FSMContext):
    data = await state.get_data()
    service = data.get("service")
    description = (data.get("description") or "").strip()

    if not description:
        await callback.answer("Сначала отправьте описание заказа!", show_alert=True)
        return

    user = callback.from_user
    photos = data.get("photos", [])
    documents = data.get("documents", [])
    skin_file = data.get("skin_file")

    if service in FIXED_PRICES:
        price = FIXED_PRICES[service]

        # Создаём заказ в базе сразу, со статусом ожидания оплаты
        number = db.create_order(
            user_id=user.id,
            username=user.username or "",
            service=service,
            description=description,
            status="waiting_payment",
            price=price,
            photos=photos,
            documents=documents,
            skin_file=skin_file,
        )

        # Уведомляем администратора о новом заказе с фиксированной ценой
        await callback.bot.send_message(
            config.ADMIN_ID,
            f"Новый заказ #{number}\n\n"
            f"Услуга: {service}\n"
            f"Цена: {price} ₽\n"
            f"Клиент: @{user.username or 'нет'}\n"
            f"ID: {user.id}\n\n"
            f"Описание:\n{description}\n\n"
            f"Статус: Ожидание оплаты от клиента."
        )

        await state.set_state(OrderState.waiting_payment)
        await state.update_data(order_number=number)

        await callback.message.edit_text(
            f"Заказ #{number}\n\n" + payment_text(service, price),
            reply_markup=payment_menu()
        )
        await callback.answer()
        return

    # Услуги с расчётом стоимости
    number = db.create_order(
        user_id=user.id,
        username=user.username or "",
        service=service,
        description=description,
        status="calculation",
        photos=photos,
        documents=documents,
        skin_file=skin_file,
    )

    text = (
        f"Новый заказ #{number}\n\n"
        f"Услуга: {service}\n"
        f"Клиент: @{user.username or 'нет'}\n"
        f"ID: {user.id}\n\n"
        f"Описание:\n{description}\n\n"
        f"Статус: Требуется расчёт цены."
    )

    await callback.bot.send_message(config.ADMIN_ID, text)

    await callback.message.edit_text(
        f"Заказ #{number} принят!\n\n"
        "Мы получили вашу заявку и рассчитаем стоимость.\n"
        "После расчёта бот автоматически отправит вам цену.",
        reply_markup=back_menu()
    )
    await state.clear()
    await callback.answer()


# ==================== ПОЛУЧЕНИЕ ОПЛАТЫ ====================

@router.message(OrderState.waiting_payment, F.photo)
async def receive_payment(message: Message, state: FSMContext):
    data = await state.get_data()
    number = data.get("order_number")
    order = db.get_order(number) if number else None

    if not order:
        # На случай потери состояния — ищем последний заказ клиента в ожидании оплаты
        order = db.get_user_order_by_status(message.from_user.id, "waiting_payment")

    caption = (
        f"Новый оплаченный заказ #{order['number'] if order else '???'}!\n\n"
        f"Услуга: {order['service'] if order else data.get('service', 'неизвестно')}\n"
        f"Клиент: @{message.from_user.username or 'нет'}\n"
        f"ID: {message.from_user.id}\n\n"
        f"Описание:\n{order['description'] if order else data.get('description', '')}"
    )

    await message.bot.send_photo(
        config.ADMIN_ID,
        photo=message.photo[-1].file_id,
        caption=caption
    )

    if order:
        db.update_status(order["number"], "paid")

    await message.answer(
        "Скрин оплаты получен!\nМы проверим оплату и начнём работу."
    )
    await state.clear()


# ==================== АДМИНСКИЕ КОМАНДЫ ====================

@router.message(Command("price"))
async def set_price(message: Message):
    """/price номер_заказа цена — установить цену для заказа с расчётом."""
    if message.from_user.id != config.ADMIN_ID:
        return

    parts = message.text.split()
    if len(parts) != 3:
        await message.answer("Использование: /price 001 750")
        return

    number, price_str = parts[1], parts[2]

    try:
        price = int(price_str)
    except ValueError:
        await message.answer("Цена должна быть числом.")
        return

    order = db.get_order(number)
    if not order:
        await message.answer("Заказ не найден.")
        return

    db.update_price(number, price)

    await message.bot.send_message(
        order["user_id"],
        "Мы рассчитали стоимость вашего заказа!\n\n"
        f"Услуга: {order['service']}\n"
        f"Цена: {price} ₽\n\n"
        "Для начала работы оплатите заказ.\n\n"
        f"Банк: {config.BANK_NAME}\n"
        f"Номер: {config.PAYMENT_NUMBER}\n"
        f"Получатель: {config.RECIPIENT_NAME}\n\n"
        "После оплаты отправьте сюда скрин чека."
    )

    await message.answer(
        f"Цена {price} ₽ отправлена клиенту для заказа #{number}."
    )


@router.message(Command("done"))
async def mark_done(message: Message):
    """/done номер_заказа — отметить заказ выполненным."""
    if message.from_user.id != config.ADMIN_ID:
        return

    parts = message.text.split()
    if len(parts) != 2:
        await message.answer("Использование: /done 001")
        return

    number = parts[1]
    order = db.get_order(number)
    if not order:
        await message.answer("Заказ не найден.")
        return

    db.update_status(number, "completed")

    await message.bot.send_message(
        order["user_id"],
        f"Ваш заказ #{number} ({order['service']}) выполнен!\n"
        "Спасибо, что выбрали нас 💛"
    )
    await message.answer(f"Заказ #{number} отмечен как выполненный.")


@router.message(Command("sendfile"))
async def prepare_sendfile(message: Message, state: FSMContext):
    """/sendfile номер_заказа — далее отправьте файл, и он уйдёт клиенту."""
    if message.from_user.id != config.ADMIN_ID:
        return

    parts = message.text.split()
    if len(parts) != 2:
        await message.answer("Использование: /sendfile 001, затем отправьте файл следующим сообщением.")
        return

    number = parts[1]
    order = db.get_order(number)
    if not order:
        await message.answer("Заказ не найден.")
        return

    await state.set_state(AdminState.waiting_file)
    await state.update_data(target_order=number, target_user=order["user_id"])
    await message.answer(f"Хорошо, жду файл для отправки клиенту по заказу #{number}.")


@router.message(AdminState.waiting_file, F.document | F.photo)
async def do_sendfile(message: Message, state: FSMContext):
    data = await state.get_data()
    number = data.get("target_order")
    user_id = data.get("target_user")

    if not user_id:
        await message.answer("Не найден заказ для отправки. Повторите /sendfile номер_заказа.")
        await state.clear()
        return

    caption = f"Ваш заказ #{number} готов! Получите файл 🎉"

    if message.document:
        await message.bot.send_document(user_id, document=message.document.file_id, caption=caption)
    elif message.photo:
        await message.bot.send_photo(user_id, photo=message.photo[-1].file_id, caption=caption)

    await message.answer(f"Файл отправлен клиенту по заказу #{number}.")
    await state.clear()


@router.message(Command("edit"))
async def request_edit(message: Message):
    """/edit номер_заказа текст — отправить заказ клиенту на правки."""
    if message.from_user.id != config.ADMIN_ID:
        return

    parts = message.text.split(maxsplit=2)
    if len(parts) != 3:
        await message.answer("Использование: /edit 001 Текст комментария по правкам")
        return

    number, comment = parts[1], parts[2]
    order = db.get_order(number)
    if not order:
        await message.answer("Заказ не найден.")
        return

    db.update_status(number, "editing")

    await message.bot.send_message(
        order["user_id"],
        f"По заказу #{number} нужны правки:\n\n{comment}\n\n"
        "Пожалуйста, ответьте в этот чат с уточнениями."
    )
    await message.answer(f"Запрос на правки отправлен клиенту по заказу #{number}.")
