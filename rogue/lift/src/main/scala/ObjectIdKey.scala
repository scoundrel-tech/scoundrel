// Copyright 2013 Foursquare Labs Inc. All Rights Reserved.

package tech.scoundrel.rogue.lift

import tech.scoundrel.mongodb.record.MongoRecord
import tech.scoundrel.mongodb.record.field.ObjectIdField
import org.bson.types.ObjectId

/**
 * Mix this into a Record to add an ObjectIdField
 */
trait ObjectIdKey[OwnerType <: MongoRecord[OwnerType]] {
  self: OwnerType =>

  object _id extends ObjectIdField(this.asInstanceOf[OwnerType])

  // convenience method that returns the value of _id
  def id: ObjectId = _id.value
}
