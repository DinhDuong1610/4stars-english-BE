package com.fourstars.FourStars.util.constant;

public enum QuestionType {
    // CHỌN ĐÁP ÁN ĐÚNG
    MULTIPLE_CHOICE_TEXT, // Câu hỏi văn bản, chọn 1 đáp án đúng (văn bản)
    MULTIPLE_CHOICE_IMAGE, // Câu hỏi văn bản, chọn 1 đáp án đúng (hình ảnh)

    // ĐIỀN VÀO CHỖ TRỐNG
    FILL_IN_BLANK, // Điền một từ còn thiếu vào câu

    // DỊCH THUẬT
    TRANSLATE_EN_TO_VI, // Dịch câu từ Anh sang Việt
    TRANSLATE_VI_TO_EN, // Dịch câu từ Việt sang Anh

    // NGHE
    LISTENING_COMPREHENSION, // Nghe một đoạn audio và chọn đáp án đúng
    LISTENING_TRANSCRIPTION, // Nghe và gõ lại chính xác những gì nghe được

    // NÓI (sử dụng AI)
    SPEAKING_PRONUNCIATION, // Đọc một câu và được chấm điểm phát âm

    // SẮP XẾP TỪ THÀNH CÂU
    ARRANGE_WORDS, // Cho các từ lộn xộn, sắp xếp lại thành câu đúng
}
