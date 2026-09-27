import json
import unittest
from pathlib import Path

from learning_pack import pack

# Room writes this file when the app is compiled (room.schemaLocation in app/build.gradle.kts).
ROOM_SCHEMA = (
    Path(__file__).resolve().parents[3]
    / "app/schemas/com.quranapp.android.learning.pack.LearningPackDatabase"
    / f"{pack.SCHEMA_VERSION}.json"
)


def room_statements(schema: dict) -> set:
    statements = set()
    for entity in schema["database"]["entities"]:
        table = entity["tableName"]
        statements.add(entity["createSql"].replace("${TABLE_NAME}", table))
        for index in entity.get("indices", []):
            statements.add(index["createSql"].replace("${TABLE_NAME}", table))
    return statements


class RoomSchemaTest(unittest.TestCase):
    """The app opens the pack with Room, which refuses tables that differ from its entities."""

    def test_pack_schema_matches_the_apps_entities(self):
        schema = json.loads(ROOM_SCHEMA.read_text(encoding="utf-8"))
        self.assertEqual(schema["database"]["version"], pack.SCHEMA_VERSION)
        self.assertEqual(set(pack.SCHEMA), room_statements(schema))


if __name__ == "__main__":
    unittest.main()
