package com.fourstars.FourStars.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dictation_sentences")
@Getter
@Setter
@NoArgsConstructor
public class DictationSentence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "audio_url", nullable = false)
    private String audioUrl;

    @Lob
    @Column(name = "correct_text", nullable = false)
    private String correctText;

    @Column(name = "order_index")
    private int orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private DictationTopic topic;
}