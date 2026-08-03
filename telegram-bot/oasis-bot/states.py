from aiogram.fsm.state import State, StatesGroup


class OrderState(StatesGroup):
    # Выбор услуги
    choosing_service = State()

    # Описание заказа
    waiting_description = State()

    # Обязательный файл (.png) для решейда и тотема
    waiting_skin_file = State()

    # Дополнительные материалы (фото, документы, архивы)
    waiting_files = State()

    # Подтверждение готовности заказа
    waiting_confirmation = State()

    # Ожидание скрина оплаты
    waiting_payment = State()


class AdminState(StatesGroup):
    # Для будущих административных функций
    waiting_price = State()
    waiting_file = State()
    waiting_edit_message = State()
