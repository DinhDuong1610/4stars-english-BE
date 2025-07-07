package com.fourstars.FourStars.messaging.dto.user;

import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long userId;
}
