package com.scgexpress.backoffice.android.db.entity.masterdata

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import java.io.Serializable

@Entity(tableName = "tbl_master_parcel_sizing")
data class TblMasterParcelSizing(
    @PrimaryKey @Json(name = "id") @SerializedName("id") @ColumnInfo(name = "id") var id: Int = 0,
    @Json(name = "status_deleted") @SerializedName("status_deleted") @ColumnInfo(name = "status_deleted") var statusDeleted: String? = "N",
    @Json(name = "status_active") @SerializedName("status_active") @ColumnInfo(name = "status_active") var statusActive: String? = "Y",
    @Json(name = "code") @SerializedName("code") @ColumnInfo(name = "code") var code: String? = "",
    @Json(name = "name") @SerializedName("name") @ColumnInfo(name = "name") var name: String? = "",
    @Json(name = "title") @SerializedName("title") @ColumnInfo(name = "title") var title: String? = "",
    @Json(name = "description") @SerializedName("description") @ColumnInfo(name = "description") var description: String? = "",
    @Json(name = "date_created") @SerializedName("date_created") @ColumnInfo(name = "date_created") var dateCreated: String? = "",
    @Json(name = "last_modified") @SerializedName("last_modified") @ColumnInfo(name = "last_modified") var lastModified: String? = ""
) : Serializable {
    @Ignore
    constructor() : this(0)
}