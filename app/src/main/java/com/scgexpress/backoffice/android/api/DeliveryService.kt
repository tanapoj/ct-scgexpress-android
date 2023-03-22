package com.scgexpress.backoffice.android.api

import com.scgexpress.backoffice.android.model.*
import io.reactivex.Flowable
import retrofit2.http.*

interface DeliveryService {
    companion object {
        private const val PARAMS_TYPE: String = "type"
        private const val PARAMS_FROM: String = "from"
        private const val PARAMS_TO: String = "to"
        private const val PARAMS_LIMIT: String = "limit"
        private const val PARAMS_WITH_BOOKING: String = "withBookings"

        private const val PARAMS_BOOKING_ID: String = "bookingID"
        private const val PARAMS_MANIFEST_ID: String = "manifestID"
        private const val PARAMS_PARCEL_LIMIT: String = "parcelLimit"
        private const val PARAMS_TRACKING_NUMBER: String = "trackingNumber"
    }

    @GET("manifests")
    fun getManifests(
        @Query(PARAMS_TYPE) type: String,
        @Query(PARAMS_FROM) from: String,
        @Query(PARAMS_TO) to: String,
        @Query(PARAMS_LIMIT) limit: String,
        @Query(PARAMS_WITH_BOOKING) withBookings: Boolean
    ): Flowable<Manifests>

    @GET("manifests/{manifestID}")
    fun getManifestDetails(
        @Path(PARAMS_MANIFEST_ID) manifestID: String,
        @Query(PARAMS_PARCEL_LIMIT) parcelLimit: String,
        @Query(PARAMS_WITH_BOOKING) withBookings: Boolean
    ): Flowable<ManifestDetail>

    @GET("parcels/{trackingNumber}")
    fun getTrackingDetails(@Path(PARAMS_TRACKING_NUMBER) trackingNumber: String): Flowable<TrackingInfo>

    @GET("getManifestItemList/{manifestID}")
    fun getManifestItemScanned(@Path(PARAMS_MANIFEST_ID) manifestID: String): Flowable<DeliveryOfdParcelResponseList>

    @GET("manifestHeader/{manifestID}")
    fun getManifestHeader(@Path(PARAMS_MANIFEST_ID) manifestID: String): Flowable<ManifestHeader>

    @GET("getTrackingNoByBooking/{bookingID}")
    fun getBookingItems(@Path(PARAMS_BOOKING_ID) bookingID: String): Flowable<List<TrackingNoByBooking>>

    @PUT("reStatus/{manifestID}")
    fun reStatus(@Path(PARAMS_MANIFEST_ID) manifestID: String, @Body body: OfdItemReStatus): Flowable<String>

    @POST("manifests")
    fun createManifest(@Body body: DeliveryOfdCreateList): Flowable<String>

    @PUT("manifests/{manifestID}/parcels")
    fun addParcels(@Path(PARAMS_MANIFEST_ID) manifestID: String, @Body body: DeliveryOfdParcelList): Flowable<DeliveryOfdParcelResponseList>

    @HTTP(method = "DELETE", path = "manifests/{manifestID}/parcels", hasBody = true)
    fun deleteParcels(@Path(PARAMS_MANIFEST_ID) manifestID: String, @Body body: DeliveryOfdParcelList): Flowable<DeliveryOfdParcelResponseList>

    @POST("acceptBooking")
    fun acceptBooking(@Body body: HashMap<String, Any>): Flowable<ApiReturnStatus>

    @POST("cancelBooking")
    fun rejectBooking(@Body body: HashMap<String, Any>): Flowable<ApiReturnStatus>

    @POST("parcelPhotos/{trackingNumber}")
    fun addPhoto(@Path(PARAMS_TRACKING_NUMBER) trackingNumber: String, @Body body: TrackingPhotoList): Flowable<String>
}