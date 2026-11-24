package com.example.cdms.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.cdms.service.ShopifySyncService;

@ExtendWith(MockitoExtension.class)
class SyncControllerTest {

    @Mock
    private ShopifySyncService shopifySyncService;

    @InjectMocks
    private SyncController syncController;

    @Test
    void triggerShopifySync_shouldCallServiceAndReturnMessage() {
        Long nullWorker = null;
        doNothing().when(shopifySyncService).syncData(1L, nullWorker);

        String result = syncController.triggerShopifySync(1L, null);

        assertEquals("Đã kích hoạt đồng bộ thành công!", result);
        verify(shopifySyncService, times(1)).syncData(1L, nullWorker);
    }
}
