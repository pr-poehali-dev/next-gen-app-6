from aiogram.fsm.storage.memory import MemoryStorage

# Единое хранилище состояний (FSM), общее для main.py и handlers.py.
# Нужно, чтобы администратор мог программно переключать состояние клиента
# (например, при команде /price перевести клиента в ожидание оплаты).
storage = MemoryStorage()
