import { Badge } from "@/components/ui/badge"

export const sections = [
  {
    id: 'hero',
    subtitle: <Badge variant="outline" className="text-green-400 border-green-400">1.19.3 • Выживание + PvP</Badge>,
    title: "FortressCraft",
    content: 'Лучший Minecraft-сервер в стиле средневековья.',
    showButton: true,
    buttonText: 'Выбрать донат'
  },
  {
    id: 'ip',
    title: 'Заходи и играй.',
    content: 'Подключайся прямо сейчас — сервер онлайн 24/7.',
    ip: 'd2.atlantix.me:25054'
  },
  {
    id: 'features',
    title: 'Что даёт донат?',
    content: 'Уникальный префикс в чате, доступ к /fly, /kit и другим командам, приоритет на вход и эксклюзивные скины для брони.'
  },
  {
    id: 'donate',
    title: 'Выбери свой ранг.',
    content: 'VIP, Elite или Legend — каждый ранг открывает новые возможности. Выдача происходит автоматически после оплаты.',
    showButton: true,
    buttonText: 'Купить привилегию'
  },
  {
    id: 'faq',
    title: 'Как это работает?',
    content: 'Выбираешь ранг → вводишь свой ник → оплачиваешь → донат выдаётся автоматически. Никаких ожиданий, никаких заявок.'
  },
]