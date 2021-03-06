package tech.scoundrel.rogue.cc

import org.scalatest.{ FlatSpec, Matchers }
import tech.scoundrel.rogue.BsonFormats._
import tech.scoundrel.rogue.map.MapKeyFormat
import org.bson.types.ObjectId
import shapeless.tag.@@
import shapeless.tag

case class CustomKey(value: Long) extends AnyVal

class MapFormatSpec extends FlatSpec with Matchers {

  case class StringMap(value: Map[String, Long])
  case class ObjectIdMap(value: Map[ObjectId, Long])

  trait M

  type ObjectIdSubtype = ObjectId @@ M

  case class ObjectIdSubtypeMap(value: Map[ObjectIdSubtype, Long])
  case class CustomKeyMap(value: Map[CustomKey, Long])

  class StringMapMeta extends RCcMetaExt[StringMap, StringMapMeta]

  "MapFormat" should "write/read string keyed map" in {

    val meta = new StringMapMeta
    val v = StringMap(Map("Hi" -> 1))
    val bson = meta.write(v)
    meta.read(bson) shouldBe v

  }

  class ObjectIdMapMeta extends RCcMetaExt[ObjectIdMap, ObjectIdMapMeta]

  it should "write/read objectId keyed map" in {

    val meta = new ObjectIdMapMeta
    val v = ObjectIdMap(Map(ObjectId.get() -> 1))
    val bson = meta.write(v)
    meta.read(bson) shouldBe v
  }

  class ObjectIdSubtypeMapMeta extends RCcMetaExt[ObjectIdSubtypeMap, ObjectIdSubtypeMapMeta]

  it should "write/read objectId subtype keyed map" in {

    val meta = new ObjectIdSubtypeMapMeta
    val v = ObjectIdSubtypeMap(Map(tag[M](ObjectId.get()) -> 1))
    val bson = meta.write(v)
    meta.read(bson) shouldBe v
  }

  implicit val customKeyMapFormat: MapKeyFormat[CustomKey] =
    MapKeyFormat[CustomKey](s => CustomKey(s.toLong), _.value.toString)

  class CustomKeyMapMeta extends RCcMetaExt[CustomKeyMap, CustomKeyMapMeta]

  it should "write/read custom keyed map" in {

    val meta = new CustomKeyMapMeta
    val v = CustomKeyMap(Map(CustomKey(1L) -> 1L))
    val bson = meta.write(v)
    meta.read(bson) shouldBe v
  }
}
