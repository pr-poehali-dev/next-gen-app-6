import { Badge } from "@/components/ui/badge"
import type { DonateRank } from "@/types"

export const donateRanks: DonateRank[] = [
  {
    id: 'king',
    name: 'Король',
    price: 5000,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/27c196c7-726e-47ed-881d-c200e45e6c7c.jpeg',
    color: '#FFD700',
    perks: ['Все привилегии', 'Префикс [Король]', '/fly, /god, /kit', 'Приоритетный вход', 'Эксклюзивный плащ']
  },
  {
    id: 'knight',
    name: 'Рыцарь',
    price: 300,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/3b10919e-196b-41ef-b969-ed5680cd989a.jpeg',
    color: '#C0C0C0',
    perks: ['Префикс [Рыцарь]', '/fly, /kit', 'Приоритетный вход', 'Броня рыцаря']
  },
  {
    id: 'mage',
    name: 'Маг',
    price: 150,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/f4680613-d861-4e83-9f90-980e3c4e067a.jpeg',
    color: '#9B59B6',
    perks: ['Префикс [Маг]', '/kit мага', 'Зелья в подарок', 'Телепорт к базе']
  },
  {
    id: 'creeper',
    name: 'Крипер',
    price: 60,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/85f0ae13-c9ea-40f4-b381-b2d6473ea6ef.jpeg',
    color: '#2ECC40',
    perks: ['Префикс [Крипер]', '/kit крипера', 'Цветной ник', 'Доступ к /tpa']
  },
  {
    id: 'skeleton',
    name: 'Скелет',
    price: 50,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/e727b3f7-eeb0-4eba-9459-6e66f6daeb89.jpeg',
    color: '#EEEEEE',
    perks: ['Префикс [Скелет]', '/kit скелета', 'Лук в подарок', 'Доступ к /tpa']
  },
  {
    id: 'zombie',
    name: 'Зомби',
    price: 50,
    image: 'https://cdn.poehali.dev/projects/7490399e-ec82-41a5-b07a-fb23138b0b97/bucket/fa5390c2-ac8d-49a2-9311-a5eece7a526f.jpeg',
    color: '#4CAF50',
    perks: ['Префикс [Зомби]', '/kit зомби', 'Зелье силы', 'Доступ к /tpa']
  },
]

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
    ranks: donateRanks,
  },
  {
    id: 'faq',
    title: 'Как это работает?',
    content: 'Выбираешь ранг → вводишь свой ник → оплачиваешь → донат выдаётся автоматически. Никаких ожиданий, никаких заявок.'
  },
]
