package slimeknights.mantle.client.book;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;

public class BookHelper {

  public static final String BOOK_COMPOUND = "mantle";
  public static final String BOOK_DATA_COMPOUND = "book";

  public static final String NBT_CURRENT_PAGE = "current_page";

  public static String getCurrentSavedPage(@Nullable ItemStack item) {
    if (item != null && !item.isEmpty() && item.has(DataComponents.CUSTOM_DATA)) {
      CompoundTag root = item.get(DataComponents.CUSTOM_DATA).copyTag();
      CompoundTag bookNBT = root.getCompound(BOOK_COMPOUND).getCompound(BOOK_DATA_COMPOUND);
      if (bookNBT.contains(NBT_CURRENT_PAGE, 8)) {
        return bookNBT.getString(NBT_CURRENT_PAGE);
      }
    }
    return "";
  }

  public static void writeSavedPageToBook(ItemStack stack, String currentPage) {
    CompoundTag root = stack.has(DataComponents.CUSTOM_DATA)
      ? stack.get(DataComponents.CUSTOM_DATA).copyTag()
      : new CompoundTag();
    CompoundTag mantleCompound = root.getCompound(BOOK_COMPOUND);
    CompoundTag bookCompound = mantleCompound.getCompound(BOOK_DATA_COMPOUND);
    bookCompound.putString(NBT_CURRENT_PAGE, currentPage);
    mantleCompound.put(BOOK_DATA_COMPOUND, bookCompound);
    root.put(BOOK_COMPOUND, mantleCompound);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
  }
}
