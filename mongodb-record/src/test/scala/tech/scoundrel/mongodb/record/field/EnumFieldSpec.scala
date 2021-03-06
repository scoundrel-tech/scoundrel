/*
 * Copyright 2010-2014 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.scoundrel
package mongodb
package record
package field

import net.liftweb.common._
import net.liftweb.json.ext.EnumSerializer
import org.specs2.mutable.Specification

import tech.scoundrel.record.field.{ EnumField, OptionalEnumField }

package enumfieldspecs {
  object WeekDay extends Enumeration {
    type WeekDay = Value
    val Mon, Tue, Wed, Thu, Fri, Sat, Sun = Value
  }

  case class JsonObj(dow: WeekDay.WeekDay) extends JsonObject[JsonObj] {
    def meta = JsonObj
  }
  object JsonObj extends JsonObjectMeta[JsonObj]

  class EnumRec extends MongoRecord[EnumRec] with ObjectIdPk[EnumRec] {
    def meta = EnumRec

    object dow extends EnumField(this, WeekDay)
    object dowOptional extends OptionalEnumField(this, WeekDay)
    object jsonobj extends JsonObjectField[EnumRec, JsonObj](this, JsonObj) {
      def defaultValue = JsonObj(WeekDay.Mon)
    }

    override def equals(other: Any): Boolean = other match {
      case that: EnumRec =>
        this.id.get == that.id.get &&
          this.dow.value == that.dow.value &&
          this.dowOptional.valueBox == that.dowOptional.valueBox &&
          this.jsonobj.value == that.jsonobj.value
      case _ => false
    }
  }
  object EnumRec extends EnumRec with MongoMetaRecord[EnumRec] {
    override def collectionName = "enumrecs"
    override def formats = super.formats + new EnumSerializer(WeekDay)
  }
}

/**
 * Systems under specification for EnumField.
 */
class EnumFieldSpec extends Specification with MongoTestKit {
  "EnumField Specification".title

  import enumfieldspecs._

  "EnumField" should {

    "work with default values" in {
      checkMongoIsRunning

      val er = EnumRec.createRecord.save()

      val erFromDb = EnumRec.find(er.id.get)
      erFromDb must beLike {
        case Full(er2) =>
          er2 mustEqual er
          er2.dow.value mustEqual WeekDay.Mon
          er2.dowOptional.valueBox mustEqual Empty
          er2.jsonobj.value mustEqual JsonObj(WeekDay.Mon)
      }
    }

    "work with set values" in {
      checkMongoIsRunning

      val er = EnumRec.createRecord
        .dow(WeekDay.Tue)
        .jsonobj(JsonObj(WeekDay.Sun))
        .save()

      val erFromDb = EnumRec.find(er.id.get)
      erFromDb must beLike {
        case Full(er2) =>
          er2 mustEqual er
          er2.dow.value mustEqual WeekDay.Tue
          er2.jsonobj.value mustEqual JsonObj(WeekDay.Sun)
      }
    }

    "work with Empty optional values" in {
      checkMongoIsRunning

      val er = EnumRec.createRecord
      er.dowOptional.setBox(Empty)
      er.save()

      val erFromDb = EnumRec.find(er.id.get)
      erFromDb must beLike {
        case Full(er2) =>
          er2 mustEqual er
          er2.dowOptional.valueBox mustEqual Empty
      }
    }

    "work with Full optional values" in {
      checkMongoIsRunning

      val er = EnumRec.createRecord
      er.dowOptional.setBox(Full(WeekDay.Sat))
      er.save()

      val erFromDb = EnumRec.find(er.id.get)
      erFromDb must beLike {
        case Full(er2) =>
          er2 mustEqual er
          er2.dowOptional.valueBox mustEqual Full(WeekDay.Sat)
      }
    }
  }
}
