package com.kseb.smart_car.data.requestDto
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class RequestSignUpDto (
    @SerialName("kakaoAccessToken")
    val kakaoAccessToken:String,
    @SerialName("nickname")
    val nickname:String,
    @SerialName("gender")
    val gender:String,
    @Serializable(with = LocalDateSerializer::class)
    @SerialName("birthday")
    val birthday: LocalDate,
    @SerialName("privacyAgreement")
    val privacyAgreement:Boolean,
    @SerialName("locationInfoAgreement")
    val locationInfoAgreement:Boolean,
    @SerialName("favoriteGenreLists")
    val favoriteGenreLists:List<String>
)

object LocalDateSerializer : KSerializer<LocalDate> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString(), formatter)
    }
}