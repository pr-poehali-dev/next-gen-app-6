import json
import os
from typing import Dict, List, Optional

DB_FILE = "orders.json"


class Database:
    def __init__(self, db_file: str = DB_FILE):
        self.db_file = db_file
        self._init_db()

    def _init_db(self):
        if not os.path.exists(self.db_file):
            with open(self.db_file, "w", encoding="utf-8") as f:
                json.dump(
                    {
                        "last_order": 0,
                        "orders": {}
                    },
                    f,
                    ensure_ascii=False,
                    indent=4
                )

    def _load(self) -> Dict:
        with open(self.db_file, "r", encoding="utf-8") as f:
            return json.load(f)

    def _save(self, data: Dict):
        with open(self.db_file, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=4)

    def generate_order_number(self) -> str:
        data = self._load()
        data["last_order"] += 1
        number = f"{data['last_order']:03d}"
        self._save(data)
        return number

    def create_order(
        self,
        user_id: int,
        username: str,
        service: str,
        description: str,
        status: str = "calculation",
        price: Optional[int] = None,
        photos: Optional[List[str]] = None,
        documents: Optional[List[str]] = None,
        skin_file: Optional[str] = None,
    ) -> str:
        data = self._load()
        number = self.generate_order_number()

        data["orders"][number] = {
            "number": number,
            "user_id": user_id,
            "username": username,
            "service": service,
            "description": description,
            "price": price,
            "status": status,
            "photos": photos or [],
            "documents": documents or [],
            "skin_file": skin_file,
        }

        self._save(data)
        return number

    def get_order(self, number: str) -> Optional[Dict]:
        data = self._load()
        return data["orders"].get(number)

    def update_price(self, number: str, price: int) -> bool:
        data = self._load()

        if number not in data["orders"]:
            return False

        data["orders"][number]["price"] = price
        data["orders"][number]["status"] = "waiting_payment"

        self._save(data)
        return True

    def update_status(self, number: str, status: str) -> bool:
        data = self._load()

        if number not in data["orders"]:
            return False

        data["orders"][number]["status"] = status
        self._save(data)
        return True

    def get_user_orders(self, user_id: int) -> List[Dict]:
        data = self._load()
        return [
            order
            for order in data["orders"].values()
            if order["user_id"] == user_id
        ]

    def get_user_order_by_status(self, user_id: int, status: str) -> Optional[Dict]:
        """Находит последний заказ пользователя с указанным статусом."""
        data = self._load()
        matching = [
            order
            for order in data["orders"].values()
            if order["user_id"] == user_id and order["status"] == status
        ]
        if not matching:
            return None
        return sorted(matching, key=lambda o: int(o["number"]))[-1]

    def get_all_orders(self) -> Dict:
        data = self._load()
        return data["orders"]


db = Database()
