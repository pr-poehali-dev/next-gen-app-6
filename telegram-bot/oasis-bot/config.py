import os
from dataclasses import dataclass

from dotenv import load_dotenv

# Загружаем переменные окружения из файла .env
load_dotenv()


@dataclass
class Config:
    # Токен Telegram-бота (берётся из .env, никогда не хранится в коде)
    BOT_TOKEN: str = os.getenv("BOT_TOKEN", "")

    # Telegram ID администратора
    ADMIN_ID: int = int(os.getenv("ADMIN_ID", "0"))

    # Название студии
    STUDIO_NAME: str = "Оазис студия"

    # Telegram-канал
    CHANNEL_URL: str = "https://t.me/Oasishj"

    # Реквизиты для оплаты
    BANK_NAME: str = "Сбер банк"
    PAYMENT_NUMBER: str = "+7 906 184 03 45"
    RECIPIENT_NAME: str = "Дарья К."


# Экземпляр конфигурации
config = Config()

if not config.BOT_TOKEN:
    raise RuntimeError(
        "BOT_TOKEN не найден! Создайте файл .env рядом с main.py и добавьте туда:\n"
        "BOT_TOKEN=ваш_токен_от_BotFather\n"
        "ADMIN_ID=ваш_telegram_id"
    )
