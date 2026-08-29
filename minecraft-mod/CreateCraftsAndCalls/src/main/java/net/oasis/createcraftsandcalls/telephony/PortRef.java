package net.oasis.createcraftsandcalls.telephony;

import net.minecraft.core.BlockPos;

/**
 * Адрес одного гнезда (порта) на телефонной станции: позиция блока станции
 * + номер гнезда внутри этого блока (0..3 — на каждом блоке станции их 4:
 * 2 пары вход/выход, см. ТЗ п.7).
 */
public record PortRef(BlockPos stationPos, int portIndex) {
}
