from aiogram.types import InlineKeyboardMarkup, InlineKeyboardButton

from config import config


def main_menu() -> InlineKeyboardMarkup:
    return InlineKeyboardMarkup(
        inline_keyboard=[
            [
                InlineKeyboardButton(
                    text="🎨 Скин — 249 ₽",
                    callback_data="service_skin"
                )
            ],
            [
                InlineKeyboardButton(
                    text="✨ Решейд скина — 149 ₽",
                    callback_data="service_reshade"
                )
            ],
            [
                InlineKeyboardButton(
                    text="🪙 Тотем по скину — 99 ₽",
                    callback_data="service_totem"
                )
            ],
            [
                InlineKeyboardButton(
                    text="🖼 Превью для видео — расчёт",
                    callback_data="service_preview"
                )
            ],
            [
                InlineKeyboardButton(
                    text="🎬 Монтаж видео — расчёт",
                    callback_data="service_editing"
                )
            ],
            [
                InlineKeyboardButton(
                    text="⚙️ Мод / плагин — расчёт",
                    callback_data="service_mod"
                )
            ],
            [
                InlineKeyboardButton(
                    text="📦 Сборка на заказ — расчёт",
                    callback_data="service_build"
                )
            ],
            [
                InlineKeyboardButton(
                    text="🏗 Постройка на заказ — расчёт",
                    callback_data="service_construction"
                )
            ],
            [
                InlineKeyboardButton(
                    text="🌐 Minecraft-сайт — расчёт",
                    callback_data="service_website"
                )
            ],
            [
                InlineKeyboardButton(
                    text="📢 Наш Telegram-канал",
                    url=config.CHANNEL_URL
                )
            ],
        ]
    )


def order_menu() -> InlineKeyboardMarkup:
    return InlineKeyboardMarkup(
        inline_keyboard=[
            [
                InlineKeyboardButton(
                    text="✅ Готово",
                    callback_data="order_done"
                )
            ],
            [
                InlineKeyboardButton(
                    text="⬅️ Назад",
                    callback_data="back_to_menu"
                )
            ],
        ]
    )


def back_menu() -> InlineKeyboardMarkup:
    return InlineKeyboardMarkup(
        inline_keyboard=[
            [
                InlineKeyboardButton(
                    text="⬅️ В главное меню",
                    callback_data="back_to_menu"
                )
            ]
        ]
    )


# Клавиатура после оформления заказа (ожидание расчёта / оплаты) —
# использует ту же разметку, что и back_menu
def payment_menu() -> InlineKeyboardMarkup:
    return back_menu()
